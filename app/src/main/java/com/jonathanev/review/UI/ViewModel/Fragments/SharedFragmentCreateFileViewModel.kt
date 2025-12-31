package com.jonathanev.review.UI.ViewModel.Fragments

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Domain.GetAttributesGuideUseCase
import com.jonathanev.review.Domain.GetContentItemsUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetVersionUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import com.jonathanev.review.Domain.SetContentUseCase
import com.jonathanev.review.Domain.SetCrearXmlUseCase
import com.jonathanev.review.Domain.SetDecodePathImageUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.data.datastore.DataStoreManager
import com.jonathanev.review.presentation.state.GuideUiState
import com.jonathanev.review.presentation.state.ResponseDomain
import com.jonathanev.review.presentation.model.ColorRangeDomain
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionItemDomain
import com.jonathanev.review.Domain.model.TypeContent
import com.jonathanev.review.UI.Utils.toDomain
import com.jonathanev.review.UI.Utils.toUi
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.provider.GuiaProvider
import com.jonathanev.review.data.xml.Versions
import com.jonathanev.review.presentation.model.QuestionContentUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setContentUseCase: SetContentUseCase,
    private val setCrearXmlUseCase: SetCrearXmlUseCase,
    private val setDecodePathImageUseCase: SetDecodePathImageUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getAttributesGuideUseCase: GetAttributesGuideUseCase,
    private val getVersionUseCase: GetVersionUseCase,
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val guiaProvider: GuiaProvider,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiStopEvent = MutableSharedFlow<UIStopEvent>()
    val uiStopEvent = _uiStopEvent.asSharedFlow()

    val dontAskDelete = dataStoreManager.getDontAskDelete()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val imageList: StateFlow<List<QuestionContentUi.Image>> = _uiState
        .map { state ->
            val currentSource =
                if (state.typeContent == TypeContent.QUESTION) state.preguntas else state.respuestas
            currentSource.getOrNull(state.contadorPregunta)
                ?.content
                ?.filterIsInstance<QuestionContentUi.Image>()
                ?: emptyList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 3. Estado Derivado: Para la lista de textos
    val textList: StateFlow<List<QuestionContentUi.Text>> = _uiState
        .map { state ->
            val currentSource =
                if (state.typeContent == TypeContent.QUESTION) state.preguntas else state.respuestas
            currentSource.getOrNull(state.contadorPregunta)
                ?.content
                ?.filterIsInstance<QuestionContentUi.Text>()
                ?: emptyList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // NO BORRAR PARA SABER COMO FUNCIONA, ME AYUDÓ A ENCONTRAR UN BUG FANTASMA
    /*init {
        viewModelScope.launch {
            _uiState.collect { state ->
                val fotos = state.preguntas.getOrNull(state.contadorPregunta)?.content?.filterIsInstance<QuestionContent.Image>()?.size ?: 0
                Log.d("DEBUG_INTERNO", "EL ESTADO MAESTRO CAMBIÓ: Preguntas size = ${state.preguntas.size}, Fotos en pregunta actual = $fotos")
            }
        }
    }*/

    fun initUIState() {
        _uiState.value = GuideUiState()
    }

    fun setColocarEtiquetas(text: String, listSpans: List<ColorRangeDomain>): String {
        return setColocarEtiquetasUseCase.invoke(text, listSpans)
    }

    fun setEditingMode(value: Boolean, position: Int) {
        _uiState.update {
            it.copy(
                isEditing = value,
                contadorContenido = position
            )
        }
    }

    fun addTextContent(textWithLabels: String, listSpans: List<ColorRangeDomain>) {
        val newContent = QuestionContentDomain.Text(textWithLabels, listSpans)

        _uiState.update { state ->
            val isQuestion = state.typeContent == TypeContent.QUESTION
            val sourceListUi = if (isQuestion) state.preguntas else state.respuestas

            // 1. Calculamos la nueva lista de preguntas/respuestas
            val updatedList = if (sourceListUi.lastIndex < state.contadorPregunta) {
                sourceListUi + QuestionItemDomain(content = listOf(newContent)).toUi()
            } else {
                val sourceListDomain = sourceListUi.map { it.toDomain() }
                val listQuestionItemDomain = setContentUseCase.invoke(
                    newContent, sourceListDomain, state.contadorPregunta,
                    state.contadorContenido, state.isEditing, QuestionContentDomain.Text::class.java
                )
                listQuestionItemDomain.map { it.toUi() }
            }

            state.copy(
                preguntas = if (isQuestion) updatedList else state.preguntas,
                respuestas = if (!isQuestion) updatedList else state.respuestas,
                isEditing = false,
                actualUri = null
            )
        }
    }

    fun addImageContent() {
        _uiState.update { currentState ->
            val uriAAgregar = currentState.actualUri?.toString() ?: return@update currentState

            val isQuestion = currentState.typeContent == TypeContent.QUESTION
            val sourceListUi = if (isQuestion) currentState.preguntas else currentState.respuestas

            val newImage = QuestionContentDomain.Image(uri = uriAAgregar, nameFile = "")

            // Usamos tu UseCase para actualizar la lista maestra
            val updatedList = if (sourceListUi.lastIndex < currentState.contadorPregunta) {
                sourceListUi + QuestionItemDomain(content = listOf(newImage)).toUi()
            } else {
                val sourceListDomain = sourceListUi.map { it.toDomain() }

                val listQuestionItemDomain = setContentUseCase.invoke(
                    newContent = newImage,
                    sourceList = sourceListDomain,
                    contadorPregunta = currentState.contadorPregunta,
                    contadorContenido = currentState.contadorContenido,
                    isEditingMode = currentState.isEditing,
                    filterType = QuestionContentDomain.Image::class.java
                )
                listQuestionItemDomain.map { it.toUi() }
            }

            // Solo retornamos las listas maestras y reseteamos flags
            currentState.copy(
                preguntas = if (isQuestion) updatedList else currentState.preguntas,
                respuestas = if (!isQuestion) updatedList else currentState.respuestas,
                isEditing = false,
                actualUri = null,
                contadorContenido = -1
            )
        }
    }

    fun setActualUri(uri: Uri) {
        _uiState.update {
            it.copy(actualUri = uri)
        }
    }

    fun deleteImage(position: Int) {
        _uiState.update { currentState ->
            val isQuestion = currentState.typeContent == TypeContent.QUESTION
            val sourceListUi = if (isQuestion) currentState.preguntas else currentState.respuestas

            // 1. Calculamos la lista actualizada usando la lógica funcional de borrado
            val sourceListToDomain = sourceListUi.map { it.toDomain() }
            val updatedListDomain = deleteFilteredContent(
                sourceList = sourceListToDomain,
                contadorPregunta = currentState.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContentDomain.Image::class.java
            )
            val updatedListToUi = updatedListDomain.map { it.toUi() }
            // 3. Emitimos el nuevo estado con todas las limpiezas integradas
            currentState.copy(
                preguntas = if (isQuestion) updatedListToUi else currentState.preguntas,
                respuestas = if (!isQuestion) updatedListToUi else currentState.respuestas,
                actualUri = null,         // resetContentLists integrado
                isEditing = false,
                contadorContenido = -1
            )
        }
    }

    fun deleteText(position: Int) {
        _uiState.update { currentState ->
            val isQuestion = currentState.typeContent == TypeContent.QUESTION
            val sourceListUi = if (isQuestion) currentState.preguntas else currentState.respuestas

            // 1. Obtenemos la lista actualizada usando la función de borrado funcional
            val sourceListToDomain = sourceListUi.map { it.toDomain() }

            val updatedListDomain = deleteFilteredContent(
                sourceList = sourceListToDomain,
                contadorPregunta = currentState.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContentDomain.Text::class.java
            )
            val updatedListToUi = updatedListDomain.map { it.toUi() }

            // 3. Emitimos el nuevo estado completo
            currentState.copy(
                preguntas = if (isQuestion) updatedListToUi else currentState.preguntas,
                respuestas = if (!isQuestion) updatedListToUi else currentState.respuestas,
                actualUri = null, // resetContentLists integrado
                isEditing = false,
                contadorContenido = -1
            )
        }
    }

    private fun deleteFilteredContent(
        sourceList: List<QuestionItemDomain>,
        contadorPregunta: Int,
        posFiltered: Int,
        filterType: Class<out QuestionContentDomain>
    ): List<QuestionItemDomain> {
        return sourceList.mapIndexed { index, item ->
            if (index == contadorPregunta) {
                // 1. Identificamos el elemento exacto dentro de la sublista filtrada
                val targetContent = item.content
                    .filter { filterType.isInstance(it) }
                    .getOrNull(posFiltered)

                // 2. Creamos una nueva lista de contenido excluyendo ese elemento específico
                // Usamos una comparación por referencia (o ID si lo tuvieras)
                val newContentList = if (targetContent != null) {
                    item.content.filter { it !== targetContent }
                } else {
                    item.content
                }

                // 3. Devolvemos el item con el contenido actualizado
                item.copy(content = newContentList)
            } else {
                // Devolvemos el item original sin cambios
                item
            }
        }
    }


    fun rollPregResp() {
        // 1. Validación de Negocio
        if (textList.value.isEmpty()) {
            sendNotification(UIStopEvent.ShowMessage("Debes tener al menos un texto"))
            return
        }

        // 2. Intercambio Atómico de Contenido
        _uiState.update { state ->
            // Determinamos el nuevo tipo (Si era QUESTION ahora es ANSWER y viceversa)
            val newType = if (state.typeContent == TypeContent.QUESTION) {
                TypeContent.ANSWER
            } else {
                TypeContent.QUESTION
            }

            state.copy(
                typeContent = newType,
                actualUri = null,           // resetContentLists integrado
                isEditing = false,          // Aseguramos que no quede en modo edición al cambiar
                contadorContenido = -1
            )
        }
    }

    fun previousQuestion() {
        val currentState = uiState.value

        // 1. Validaciones de Negocio
        if (currentState.contadorPregunta == 0) {
            sendNotification(UIStopEvent.NotQuestionBefore("Ya no hay preguntas anteriores"))
            return
        }

        if (textList.value.isEmpty()) {
            sendNotification(UIStopEvent.ShowMessage("Debes tener al menos un texto"))
            return
        }

        // Validar contraparte (si estoy en pregunta, validar que la respuesta tenga texto)
        val listToCheckUi = if (currentState.typeContent == TypeContent.QUESTION)
            currentState.respuestas
        else
            currentState.preguntas

        val listCheckToDomain = listToCheckUi.map { it.toDomain() }
        val hasCounterpartText = listCheckToDomain.getOrNull(currentState.contadorPregunta)?.content
            ?.any { it is QuestionContentDomain.Text } ?: false

        if (!hasCounterpartText) {
            emitShowMessage() // Mantiene tu lógica de mostrar mensaje si no hay contraparte
            return
        }

        // 2. Transición al estado anterior
        _uiState.update { state ->
            val nuevoContador = state.contadorPregunta - 1

            state.copy(
                contadorPregunta = nuevoContador,
                typeContent = TypeContent.QUESTION, // Por estándar, volver a mostrar la Pregunta
                actualUri = null,           // Limpieza de datos temporales
                isEditing = false,
                contadorContenido = -1
            )
        }
    }

    private fun sendNotification(event: UIStopEvent) {
        viewModelScope.launch {
            _uiStopEvent.emit(event)
        }
    }

    fun nextQuestion() {
        val currentState = uiState.value

        if (textList.value.isEmpty()) {
            sendNotification(UIStopEvent.ShowMessage("Debes tener al menos un texto"))
            return
        }

        val listToCheckUi = if (currentState.typeContent == TypeContent.QUESTION)
            currentState.respuestas
        else
            currentState.preguntas
        val listCheckToDomain = listToCheckUi.map { it.toDomain() }

        val hasCounterpart = listCheckToDomain.getOrNull(currentState.contadorPregunta)?.content
            ?.any { it is QuestionContentDomain.Text } ?: false

        if (!hasCounterpart) {
            emitShowMessage()
            return
        }

        val isLastQuestion =
            currentState.contadorPregunta + 1 >= currentState.respuestas.size

        if (isLastQuestion) {
            viewModelScope.launch {
                _uiStopEvent.emit(UIStopEvent.AddMoreQuestions("Ya no hay mas preguntas, ¿quieres agregar mas?"))
            }
            return
        }

        advanceToNextQuestion()
    }

    fun advanceToNextQuestion() {
        _uiState.update { state ->
            state.copy(
                contadorPregunta = state.contadorPregunta + 1,
                typeContent = TypeContent.QUESTION, // Siempre volvemos a QUESTION al avanzar
                actualUri = null,           // resetContentLists integrado
                isEditing = false,
                contadorContenido = -1
            )
        }
    }

    private fun emitShowMessage() {
        viewModelScope.launch {
            _uiStopEvent.emit(UIStopEvent.ShowMessage("Debes tener al menos un texto en pregunta/respuesta"))
        }
    }

    fun getCurrentPath() = fileRepository.getCurrentPath()

    fun getObtenerDatosXML(positionContent: Int) {
        val currentState = _uiState.value

        if (currentState.respuestas.isEmpty()) {
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            // 1. Mapeamos las listas completas desde el XML
            val newQuestionsDomain = datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }
            val newAnswersDomain = datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }
            val newQuestionsToUi = newQuestionsDomain.map { it.toUi() }
            val newAnswersToUi = newAnswersDomain.map { it.toUi() }

            _uiState.update { state ->
                state.copy(
                    contadorPregunta = positionContent,
                    typeContent = TypeContent.QUESTION,
                    preguntas = newQuestionsToUi,
                    respuestas = newAnswersToUi,
                )
            }
        }
    }

    private fun isDataValid(): Boolean {
        val stateUi = uiState.value
        val preguntasDomain = stateUi.preguntas.map { it.toDomain() }
        val respuestasDomain = stateUi.respuestas.map { it.toDomain() }

        val questionHasContent = preguntasDomain.isEmpty()
        if (questionHasContent) {
            sendNotification(
                UIStopEvent.ShowMessage("Debes tener minimo algo para guardar")
            )
            return false
        }

        // Validar consistencia en la posición actual
        val currentQuestionHasText =
            preguntasDomain.getOrNull(stateUi.contadorPregunta)?.hasText() ?: false
        val currentAnswerHasText =
            respuestasDomain.getOrNull(stateUi.contadorPregunta)?.hasText() ?: false

        if (!currentQuestionHasText || !currentAnswerHasText) {
            // Usamos el mensaje genérico que ya tenías definido
            emitShowMessage()
            return false
        }

        return true
    }

    // Extension function para limpiar el código de las listas
    private fun QuestionItemDomain.hasText(): Boolean {
        return this.content.any { it is QuestionContentDomain.Text }
    }

    fun saveOldGuide() {
        if (!isDataValid()) {
            return
        }

        viewModelScope.launch {
            val currentPath = File(getCurrentPath())
            val preguntasDomain = uiState.value.preguntas.map { it.toDomain() }
            val respuestasDomain = uiState.value.respuestas.map { it.toDomain() }

            // 1. Transformación de datos (Dominio)
            // Generamos nombres de archivo antes de cualquier operación de archivos
            val (preguntasProcesadas, respuestasProcesadas) = setDecodePathImageUseCase.invoke(
               preguntasDomain,
               respuestasDomain
            )

            val preguntasProcesadasUi = preguntasProcesadas.map { it.toUi() }
            val respuestasProcesadasUi = respuestasProcesadas.map { it.toUi() }

            // Actualizamos estado para que la UI y el proceso de guardado usen lo mismo
            _uiState.update {
                it.copy(
                    preguntas = preguntasProcesadasUi,
                    respuestas =respuestasProcesadasUi
                )
            }

            // 2. Preparación de rutas
            val version = getVersionUseCase.invoke(currentPath)
            val basePath = currentPath.toString().replace(".xml", "")
            val imagesFolder = File(basePath.replace("guias", "imagenes"))

            if (!imagesFolder.exists()) {
                imagesFolder.mkdir()
            }

            // 3. Gestión de Imágenes (Lógica unificada)
            // Determinamos la carpeta de búsqueda según versión (V1 usaba el parent)
            val searchFolder = if (version == Versions.VERSION1) imagesFolder.parentFile else imagesFolder

            val allContent = preguntasProcesadas + respuestasProcesadas
            val listImagesXML =
                allContent.flatMap { it.content }.filterIsInstance<QuestionContentDomain.Image>()

            // B. Identificar faltantes
            var currentDeviceNames =
                searchFolder?.listFiles()?.map { it.name }?.toSet() ?: emptySet()

            // D. Guardar nuevas imágenes
            val imagesToDownload = listImagesXML.filter { it.nameFile !in currentDeviceNames }
            if (imagesToDownload.isNotEmpty()) {
                guiaProvider.saveImagesInDevice(imagesToDownload, imagesFolder)
            }

            // C. Mover imagenes
            if (version == Versions.VERSION1) {
                listImagesXML.filter { it.nameFile in currentDeviceNames }.forEach { image ->
                    val destination = File(imagesFolder, image.nameFile)

                    Files.move(
                        Paths.get(image.uri),
                        Paths.get(destination.path),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            }

            // Borrar imagenes que ya no estén en el XML pero si en el dispositivo
            currentDeviceNames = imagesFolder.listFiles()?.map { it.name }?.toSet() ?: emptySet()
            val listDelete = currentDeviceNames - listImagesXML.map { it.nameFile }.toSet()

            listDelete.forEach { image ->
                val destination = File(imagesFolder, image)
                if (destination.exists() && destination.isFile) {
                    destination.delete()
                }
            }


            // 4. Persistencia Final del XML
            val response = getAttributesGuideUseCase.invoke(currentPath)
            val isSuccess = setCrearXmlUseCase.invoke(
                response.nameGuide,
                response.description,
                currentPath,
                preguntasProcesadas,
                respuestasProcesadas
            )

            // 5. Evento de cierre
            if (isSuccess) {
                initUIState()
                fileRepository.setCurrentPath(filePathsProvider.fileGuides.path)
                _uiStopEvent.emit(UIStopEvent.GuideCreatedSuccess("Se ha guardado la guia correctamente"))
            }
        }
    }

    fun saveNewGuide(nameGuide: String, description: String) {
        // 1. Validaciones previas (Capa de Presentación)
        if (!isDataValid()) {
            return
        }

        viewModelScope.launch {
            val preguntasDomain = uiState.value.preguntas.map { it.toDomain() }
            val respuestasDomain = uiState.value.respuestas.map { it.toDomain() }

            // 2. Transformación de datos (Capa de Dominio)
            // Obtenemos las listas con los nombres de imagen ya generados (1.png, 2.png, etc.)
            val (preguntasProcesadas, respuestasProcesadas) = setDecodePathImageUseCase.invoke(
                preguntasDomain,
                respuestasDomain
            )

            val preguntasProcesadasUi = preguntasProcesadas.map { it.toUi() }
            val respuestasProcesadasUi = respuestasProcesadas.map { it.toUi() }

            // 3. Actualizamos el estado de la UI para que coincida con lo que vamos a guardar
            _uiState.update {
                it.copy(
                    preguntas = preguntasProcesadasUi,
                    respuestas = respuestasProcesadasUi
                )
            }

            // 4. Preparación de Archivos (Lógica de IO)
            val currentPath = filePathsProvider.buildFile(File(getCurrentPath()), nameGuide)
            val imagesFolder = File(
                filePathsProvider.buildFolder(File(getCurrentPath()), nameGuide).path
                    .replace("guias", "imagenes")
            )

            // Limpieza y creación de directorio de imágenes
            if (imagesFolder.exists()) imagesFolder.deleteRecursively()
            imagesFolder.mkdir()

            // 5. Persistencia de Imágenes
            // Usamos las listas PROCESADAS, no las del state viejo
            val listImages = (preguntasProcesadas + respuestasProcesadas)
                .flatMap { it.content }
                .filterIsInstance<QuestionContentDomain.Image>()

            if (listImages.isNotEmpty()) {
                guiaProvider.saveImagesInDevice(listImages, imagesFolder)
            }

            // 6. Persistencia del XML
            val isSuccess = setCrearXmlUseCase.invoke(
                nameGuide,
                description,
                currentPath,
                preguntasProcesadas, // IMPORTANTE: Usar los datos actualizados
                respuestasProcesadas
            )

            // 7. Notificación de resultado y limpieza de navegación
            if (isSuccess) {
                initUIState()
                fileRepository.setCurrentPath(filePathsProvider.fileGuides.path)
                _uiStopEvent.emit(
                    UIStopEvent.GuideCreatedSuccess("Se ha guardado la guía correctamente")
                )
            }
        }
    }

    fun deleteQuesAns() {
        _uiState.update { state ->
            val index = state.contadorPregunta

            // 1. Eliminamos pregunta y respuesta en la misma posición
            val newPreguntas = state.preguntas.toMutableList().apply {
                if (index in indices) removeAt(index)
            }

            val newRespuestas = state.respuestas.toMutableList().apply {
                if (index in indices) removeAt(index)
            }

            // 2. Si ya no queda nada → limpiar todo
            if (newPreguntas.isEmpty()) {
                return@update state.copy(
                    preguntas = emptyList(),
                    respuestas = emptyList(),
                    contadorPregunta = 0,
                    contadorContenido = 0,
                    isEditing = false,
                    actualUri = null
                )
            }

            // 3. Calcular nuevo índice
            val newIndex = if (index > 0) index - 1 else 0

            state.copy(
                preguntas = newPreguntas,
                respuestas = newRespuestas,
                contadorPregunta = newIndex,
                contadorContenido = 0,
                isEditing = false,
                actualUri = null
            )
        }
    }

    fun saveDontAskDelete() {
        viewModelScope.launch {
            dataStoreManager.setDontAskDelete(true)
            val value = dataStoreManager.getDontAskDelete().first()
            Log.d("DATASTORE", "DontAskDelete = $value")
        }
    }

    suspend fun getDontAskDeleteOnce() = dataStoreManager
        .getDontAskDelete()
        .first()

}