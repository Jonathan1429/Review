package com.jonathanev.review.UI.ViewModel.Fragments

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Core.Constants.VERSION1
import com.jonathanev.review.Data.Model.GuideUiState
import com.jonathanev.review.Data.Model.prueba.AnswerState
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Domain.GetAttributesGuideUseCase
import com.jonathanev.review.Domain.GetContentItemsUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetVersionUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import com.jonathanev.review.Domain.SetContentUseCase
import com.jonathanev.review.Domain.SetCrearXmlUseCase
import com.jonathanev.review.Domain.SetDecodePathImageUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getContentItemsUseCase: GetContentItemsUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getAttributesGuideUseCase: GetAttributesGuideUseCase,
    private val getVersionUseCase: GetVersionUseCase,
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val guiaProvider: GuiaProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiStopEvent = MutableSharedFlow<UIStopEvent>()
    val uiStopEvent = _uiStopEvent.asSharedFlow()

    val imageList: StateFlow<List<QuestionContent.Image>> = _uiState
        .map { state ->
            val currentSource =
                if (state.typeContent == TypeContent.QUESTION) state.preguntas else state.respuestas
            currentSource.getOrNull(state.contadorPregunta)
                ?.content
                ?.filterIsInstance<QuestionContent.Image>()
                ?: emptyList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // 3. Estado Derivado: Para la lista de textos
    val textList: StateFlow<List<QuestionContent.Text>> = _uiState
        .map { state ->
            val currentSource =
                if (state.typeContent == TypeContent.QUESTION) state.preguntas else state.respuestas
            currentSource.getOrNull(state.contadorPregunta)
                ?.content
                ?.filterIsInstance<QuestionContent.Text>()
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

    fun setColocarEtiquetas(text: String, listSpans: List<ColorRange>): String {
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

    fun addTextContent(textWithLabels: String, listSpans: List<ColorRange>) {
        val newContent = QuestionContent.Text(textWithLabels, listSpans)

        _uiState.update { state ->
            val isQuestion = state.typeContent == TypeContent.QUESTION
            val sourceList = if (isQuestion) state.preguntas else state.respuestas

            // 1. Calculamos la nueva lista de preguntas/respuestas
            val updatedList = if (sourceList.lastIndex < state.contadorPregunta) {
                sourceList + QuestionItem(content = listOf(newContent))
            } else {
                setContentUseCase.invoke(
                    newContent, sourceList, state.contadorPregunta,
                    state.contadorContenido, state.isEditing, QuestionContent.Text::class.java
                )
            }

            // 2. Extraemos el contenido que se debe pintar ahora mismo (lo que hacía showContents)
            // Usamos la lista actualizada para obtener el item actual
            val currentItem = updatedList.getOrNull(state.contadorPregunta)
            val (newTextList, newImageList) = if (currentItem != null) {
                getContentItemsUseCase.invoke(updatedList, state.contadorPregunta)
            } else {
                Pair(emptyList(), emptyList())
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
            val sourceList = if (isQuestion) currentState.preguntas else currentState.respuestas

            val newImage = QuestionContent.Image(uri = uriAAgregar, nameFile = "")

            // Usamos tu UseCase para actualizar la lista maestra
            val updatedList = if (sourceList.lastIndex < currentState.contadorPregunta) {
                sourceList + QuestionItem(content = listOf(newImage))
            } else {
                setContentUseCase.invoke(
                    newContent = newImage,
                    sourceList = sourceList,
                    contadorPregunta = currentState.contadorPregunta,
                    contadorContenido = currentState.contadorContenido,
                    isEditingMode = currentState.isEditing,
                    filterType = QuestionContent.Image::class.java
                )
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
            val sourceList = if (isQuestion) currentState.preguntas else currentState.respuestas

            // 1. Calculamos la lista actualizada usando la lógica funcional de borrado
            val updatedList = deleteFilteredContent(
                sourceList = sourceList,
                contadorPregunta = currentState.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContent.Image::class.java
            )

            // 2. Sincronizamos lo que la UI debe mostrar (Reemplaza a showContents)
            // Usamos updatedList para que el cambio sea inmediato en los adaptadores
            val responseContent = getContentItemsUseCase.invoke(
                updatedList,
                currentState.contadorPregunta
            )

            // 3. Emitimos el nuevo estado con todas las limpiezas integradas
            currentState.copy(
                preguntas = if (isQuestion) updatedList else currentState.preguntas,
                respuestas = if (!isQuestion) updatedList else currentState.respuestas,
                actualUri = null,         // resetContentLists integrado
                isEditing = false,
                contadorContenido = -1
            )
        }
    }

    fun deleteText(position: Int) {
        _uiState.update { currentState ->
            val isQuestion = currentState.typeContent == TypeContent.QUESTION
            val sourceList = if (isQuestion) currentState.preguntas else currentState.respuestas

            // 1. Obtenemos la lista actualizada usando la función de borrado funcional
            val updatedList = deleteFilteredContent(
                sourceList = sourceList,
                contadorPregunta = currentState.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContent.Text::class.java
            )

            // 2. Recalculamos el contenido que debe ver la UI (Reemplaza a showContents)
            // Es vital pasar la 'updatedList' para que el cambio sea visible de inmediato
            val responseContent = getContentItemsUseCase.invoke(
                updatedList,
                currentState.contadorPregunta
            )

            // 3. Emitimos el nuevo estado completo
            currentState.copy(
                preguntas = if (isQuestion) updatedList else currentState.preguntas,
                respuestas = if (!isQuestion) updatedList else currentState.respuestas,
                actualUri = null, // resetContentLists integrado
                isEditing = false,
                contadorContenido = -1
            )
        }
    }

    private fun deleteFilteredContent(
        sourceList: List<QuestionItem>,
        contadorPregunta: Int,
        posFiltered: Int,
        filterType: Class<out QuestionContent>
    ): List<QuestionItem> {
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

            /*// Obtenemos qué listas mostrar según el nuevo tipo
            val sourceList =
                if (newType == TypeContent.QUESTION) state.preguntas else state.respuestas*/

            // Obtenemos el contenido para la UI (Reemplaza a showContents)
            //val responseContent = getContentItemsUseCase.invoke(sourceList, state.contadorPregunta)

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
        val listToCheck = if (currentState.typeContent == TypeContent.QUESTION)
            currentState.respuestas
        else
            currentState.preguntas

        val hasCounterpartText = listToCheck.getOrNull(currentState.contadorPregunta)?.content
            ?.any { it is QuestionContent.Text } ?: false

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

        val listToCheck = if (currentState.typeContent == TypeContent.QUESTION)
            currentState.respuestas
        else
            currentState.preguntas

        val hasCounterpart = listToCheck.getOrNull(currentState.contadorPregunta)?.content
            ?.any { it is QuestionContent.Text } ?: false

        if (!hasCounterpart) {
            emitShowMessage()
            return
        }

        val isLastQuestion =
            currentState.contadorPregunta + 1 >= currentState.respuestas.size

        if (isLastQuestion){
            viewModelScope.launch {
                _uiStopEvent.emit(UIStopEvent.AddMoreQuestions("Ya no hay mas preguntas, ¿quieres agregar mas?"))
            }
            return
        }

        advanceToNextQuestion()
    }

    fun advanceToNextQuestion(){
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
            val nuevasPreguntas = datos.map { it.question }
            val nuevasRespuestas = datos.mapNotNull { (it.answer as? AnswerState.Filled)?.item }

            _uiState.update { state ->
                state.copy(
                    contadorPregunta = positionContent,
                    typeContent = TypeContent.QUESTION,
                    preguntas = nuevasPreguntas,
                    respuestas = nuevasRespuestas,
                )
            }
        }
    }

    private fun isDataValid(): Boolean {
        val state = uiState.value

        val a = state.preguntas.getOrNull(state.contadorPregunta)?.content?.size ?: 0
        if (a == 0){
            return true
        }

        // Validar consistencia en la posición actual
        val currentQuestionHasText =
            state.preguntas.getOrNull(state.contadorPregunta)?.hasText() ?: false
        val currentAnswerHasText =
            state.respuestas.getOrNull(state.contadorPregunta)?.hasText() ?: false

        if (!currentQuestionHasText || !currentAnswerHasText) {
            // Usamos el mensaje genérico que ya tenías definido
            emitShowMessage()
            return false
        }

        return true
    }

    // Extension function para limpiar el código de las listas
    private fun QuestionItem.hasText(): Boolean {
        return this.content.any { it is QuestionContent.Text }
    }

    fun saveOldGuide() {
        if (!isDataValid()) {
            return
        }

        viewModelScope.launch {
            val currentPath = File(getCurrentPath())

            // 1. Transformación de datos (Dominio)
            // Generamos nombres de archivo antes de cualquier operación de archivos
            val (preguntasProcesadas, respuestasProcesadas) = setDecodePathImageUseCase(
                uiState.value.preguntas,
                uiState.value.respuestas
            )

            // Actualizamos estado para que la UI y el proceso de guardado usen lo mismo
            _uiState.update {
                it.copy(
                    preguntas = preguntasProcesadas,
                    respuestas = respuestasProcesadas
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
            val searchFolder = if (version == VERSION1) imagesFolder.parentFile else imagesFolder

            val allContent = preguntasProcesadas + respuestasProcesadas
            val listImagesXML =
                allContent.flatMap { it.content }.filterIsInstance<QuestionContent.Image>()

            // B. Identificar faltantes
            var currentDeviceNames =
                searchFolder?.listFiles()?.map { it.name }?.toSet() ?: emptySet()

            // D. Guardar nuevas imágenes
            val imagesToDownload = listImagesXML.filter { it.nameFile !in currentDeviceNames }
            if (imagesToDownload.isNotEmpty()) {
                guiaProvider.saveImagesInDevice(imagesToDownload, imagesFolder)
            }

            // C. Mover imagenes
            if (version == VERSION1) {
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
                if (destination.exists() && destination.isFile){
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
            // 2. Transformación de datos (Capa de Dominio)
            // Obtenemos las listas con los nombres de imagen ya generados (1.png, 2.png, etc.)
            val (preguntasProcesadas, respuestasProcesadas) = setDecodePathImageUseCase(
                uiState.value.preguntas,
                uiState.value.respuestas
            )

            // 3. Actualizamos el estado de la UI para que coincida con lo que vamos a guardar
            _uiState.update {
                it.copy(
                    preguntas = preguntasProcesadas,
                    respuestas = respuestasProcesadas
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
                .filterIsInstance<QuestionContent.Image>()

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
}