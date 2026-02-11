package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.GetSaveGuidesUseCase
import com.jonathanev.review.domain.SetContentUseCase
import com.jonathanev.review.domain.SetCrearXmlUseCase
import com.jonathanev.review.domain.mapper.GuideQuestionExtractor
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.UserPreferencesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.SaveGuideError
import com.jonathanev.review.domain.result.UpdateGuideResult
import com.jonathanev.review.presentation.event.CreateGuideEvent
import com.jonathanev.review.presentation.event.CreateGuideEvent.ShowMessage
import com.jonathanev.review.presentation.event.CreateGuideEvent.SuccessGuideCreated
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.state.GuideUiState
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
import javax.inject.Inject

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setContentUseCase: SetContentUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val getSaveGuidesUseCase: GetSaveGuidesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val guideQuestionExtractor: GuideQuestionExtractor,
    private val setCrearXmlUseCase: SetCrearXmlUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

    private val _createGuideEvent = MutableSharedFlow<CreateGuideEvent>()
    val createGuideEvent = _createGuideEvent.asSharedFlow()

    val imageList: StateFlow<List<QuestionContentUi.Image>> = _uiState
        .map { state ->
            val currentSource =
                if (state.qAType == QAType.QUESTION) state.preguntas else state.respuestas
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
                if (state.qAType == QAType.QUESTION) state.preguntas else state.respuestas
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

    fun setEditingMode(value: Boolean, position: Int) {
        _uiState.update {
            it.copy(
                isEditing = value,
                contadorContenido = position
            )
        }
    }

    fun addTextContent(textWithLabels: String, listSpans: List<ColorRangeUi>) {
        val listSpansDomain = listSpans.map { it.toDomain() }
        val newContent = QuestionContentDomain.Text(textWithLabels, listSpansDomain)

        _uiState.update { state ->
            val isQuestion = state.qAType == QAType.QUESTION
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
            val uriAAgregar = currentState.actualUri ?: return@update currentState

            val isQuestion = currentState.qAType == QAType.QUESTION
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

    fun setActualUri(uri: String) {
        _uiState.update {
            it.copy(actualUri = uri)
        }
    }

    fun deleteImage(position: Int) {
        _uiState.update { currentState ->
            val isQuestion = currentState.qAType == QAType.QUESTION
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
            val isQuestion = currentState.qAType == QAType.QUESTION
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
            sendNotification(CreateGuideEvent.WithoutText)
            return
        }

        // 2. Intercambio Atómico de Contenido
        _uiState.update { state ->
            // Determinamos el nuevo tipo (Si era QUESTION ahora es ANSWER y viceversa)
            val newType = if (state.qAType == QAType.QUESTION) {
                QAType.ANSWER
            } else {
                QAType.QUESTION
            }

            state.copy(
                qAType = newType,
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
            sendNotification(CreateGuideEvent.NotQuestionBefore)
            return
        }

        if (textList.value.isEmpty()) {
            sendNotification(CreateGuideEvent.WithoutText)
            return
        }

        // Validar contraparte (si estoy en pregunta, validar que la respuesta tenga texto)
        val listToCheckUi = if (currentState.qAType == QAType.QUESTION)
            currentState.respuestas
        else
            currentState.preguntas

        val listCheckToDomain = listToCheckUi.map { it.toDomain() }
        val hasCounterpartText = listCheckToDomain.getOrNull(currentState.contadorPregunta)?.content
            ?.any { it is QuestionContentDomain.Text } ?: false

        if (!hasCounterpartText) {
            sendNotification(CreateGuideEvent.WithoutTextQA)
            return
        }

        // 2. Transición al estado anterior
        _uiState.update { state ->
            val nuevoContador = state.contadorPregunta - 1

            state.copy(
                contadorPregunta = nuevoContador,
                qAType = QAType.QUESTION, // Por estándar, volver a mostrar la Pregunta
                actualUri = null,           // Limpieza de datos temporales
                isEditing = false,
                contadorContenido = -1
            )
        }
    }

    private fun sendNotification(event: CreateGuideEvent) {
        viewModelScope.launch {
            _createGuideEvent.emit(event)
        }
    }

    private fun showMessage(text: String) {
        viewModelScope.launch {
            _createGuideEvent.emit(CreateGuideEvent.ShowMessage(text))
        }
    }

    fun nextQuestion() {
        val currentState = uiState.value

        if (textList.value.isEmpty()) {
            sendNotification(CreateGuideEvent.WithoutText)
            return
        }

        val listToCheckUi = if (currentState.qAType == QAType.QUESTION)
            currentState.respuestas
        else
            currentState.preguntas
        val listCheckToDomain = listToCheckUi.map { it.toDomain() }

        val hasCounterpart = listCheckToDomain.getOrNull(currentState.contadorPregunta)?.content
            ?.any { it is QuestionContentDomain.Text } ?: false

        if (!hasCounterpart) {
            sendNotification(CreateGuideEvent.WithoutTextQA)
            return
        }

        var isLastQuestion = false
        if (currentState.isLastQuestion == null) {
            isLastQuestion =
                currentState.contadorPregunta + 1 == currentState.respuestas.size
        }

        if (isLastQuestion) {
            sendNotification(CreateGuideEvent.AddMoreQuestions)
            return
        }

        _uiState.update { state ->
            state.copy(
                contadorPregunta = state.contadorPregunta + 1,
                qAType = QAType.QUESTION, // Siempre volvemos a QUESTION al avanzar
                actualUri = null,           // resetContentLists integrado
                isEditing = false,
                contadorContenido = -1,
            )
        }
    }

    private fun findGuide(nameGuide: String): GuideDomainModel? =
        getSaveGuidesUseCase.invoke().find { it.nameGuide == nameGuide }

    private fun loadGuideXml(
        guide: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ): GetGuideResult =
        getGuideXmlDataUseCase.invoke(GuideContext.Editing(guide, relativeGuidePath))

    private fun handleGuideResult(
        result: GetGuideResult,
        positionContent: Int
    ) {
        when (result) {
            is GetGuideResult.Success -> updateUiWithContent(result, positionContent)
            GetGuideResult.Error ->
                showMessage("Ocurrió un error al abrir la guia")

            GetGuideResult.InvalidFormat -> showMessage("La guia está dañada")
            GetGuideResult.NotFound -> showMessage("No se ha encontrado la guia")
            GetGuideResult.UnknownError -> showMessage("Error desconocido")
        }
    }


    private fun updateUiWithContent(
        result: GetGuideResult.Success,
        positionContent: Int
    ) {
        val (questions, answers) = guideQuestionExtractor.map(result)

        _uiState.update { state ->
            state.copy(
                contadorPregunta = calculatePosition(positionContent, answers.size),
                qAType = QAType.QUESTION,
                preguntas = questions.map { it.toUi() },
                respuestas = answers.map { it.toUi() },
                isLastQuestion = if (positionContent == -1) false else null
            )
        }
    }

    private fun calculatePosition(position: Int, totalAnswers: Int): Int =
        if (position == -1) totalAnswers else position

    fun getObtenerDatosXML(
        positionContent: Int,
        nameGuide: String,
        relativeGuidePath: RelativeGuidePath
    ) {
        if (uiState.value.respuestas.isNotEmpty()) return

        val guide = findGuide(nameGuide) ?: run {
            showMessage("No se ha encontrado la guia a renombrar")
            return
        }

        val result = loadGuideXml(guide, relativeGuidePath)
        handleGuideResult(result, positionContent)
    }

    private fun isDataValid(): Boolean {
        val stateUi = uiState.value
        val preguntasDomain = stateUi.preguntas.map { it.toDomain() }
        val respuestasDomain = stateUi.respuestas.map { it.toDomain() }

        val questionHasContent = preguntasDomain.isEmpty()
        if (questionHasContent) {
            showMessage("Debes tener minimo algo para guardar")
            return false
        }

        // Validar consistencia en la posición actual
        val currentQuestionHasText =
            preguntasDomain.getOrNull(stateUi.contadorPregunta)?.hasText() ?: false
        val currentAnswerHasText =
            respuestasDomain.getOrNull(stateUi.contadorPregunta)?.hasText() ?: false

        if (!currentQuestionHasText || !currentAnswerHasText) {
            // Usamos el mensaje genérico que ya tenías definido
            sendNotification(CreateGuideEvent.WithoutTextQA)
            return false
        }

        return true
    }

    // Extension function para limpiar el código de las listas
    private fun QuestionItemDomain.hasText(): Boolean {
        return this.content.any { it is QuestionContentDomain.Text }
    }

    fun saveGuide(
        nameGuide: String,
        description: String,
        relativeGuidePath: RelativeGuidePath,
        mode: SaveGuideMode
    ) {
        if (!isDataValid()) {
            return
        }

        viewModelScope.launch {
            val response = setCrearXmlUseCase.invoke(
                nameGuide = nameGuide,
                description = description,
                preguntas = uiState.value.preguntas.map { it.toDomain() },
                respuestas = uiState.value.respuestas.map { it.toDomain() },
                relativeGuidePath = relativeGuidePath,
                mode = mode
            )

            when (response) {
                UpdateGuideResult.ErrorPath ->
                    sendNotification(ShowMessage("Error en la ruta inicial"))

                UpdateGuideResult.ErrorUpdateGuide ->
                    sendNotification(ShowMessage("Error al cargar datos de la guia"))

                UpdateGuideResult.ImagesFailed ->
                    sendNotification(SuccessGuideCreated("Guia guardada con imagenes corruptas"))

                is UpdateGuideResult.SaveFailed -> {
                    when (response.cause) {
                        SaveGuideError.ErrorSave ->
                            sendNotification(ShowMessage("Error al guardar la guia"))

                        SaveGuideError.IOException ->
                            sendNotification(ShowMessage("Error de entrada/salida al guardar la guía"))

                        SaveGuideError.SecurityException ->
                            sendNotification(ShowMessage("Permisos insuficientes para guardar la guía"))
                    }
                }

                UpdateGuideResult.Success ->
                    sendNotification(
                        SuccessGuideCreated("Guia guardada satisfactoriamente")
                    )
            }
        }
    }

    /*fun saveNewGuide(
        nameGuide: String,
        description: String,
        relativeGuidePath: RelativeGuidePath
    ) {
        // 1. Validaciones previas (Capa de Presentación)
        if (!isDataValid()) {
            return
        }

        viewModelScope.launch {
            val preguntasDomain = uiState.value.preguntas.map { it.toDomain() }
            val respuestasDomain = uiState.value.respuestas.map { it.toDomain() }

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

            val guides = loadGuidesUseCase.invoke(relativeGuidePath)
            val guide = guides.find { it.nameGuide == nameGuide }

            updateImagesUseCase.invoke(
                guideDomain = guide ?: GuideDomainModel(GuideVersion.V2, nameGuide, description),
                preguntasProcesadas = preguntasProcesadas,
                respuestasProcesadas = respuestasProcesadas,
                isNewFile = true,
                relativeGuidePath = relativeGuidePath
            )

            val dataWithTags =
                setLabelsUseCase.invoke(preguntasProcesadas, respuestasProcesadas)

            // 6. Persistencia del XML
            val isSuccess = setCrearXmlUseCase.invoke(
                nameGuide = nameGuide,
                description = description,
                version = GuideVersion.V2,
                preguntas = dataWithTags.first,
                respuestas = dataWithTags.second,
                relativeGuidePath = relativeGuidePath
            )

            // 7. Notificación de resultado y limpieza de navegación
            if (isSuccess) {
                initUIState()
                sendNotification(SuccessGuideCreated)
            } else {
                sendNotification(ErrorGuideCreated)
            }
        }
    }*/

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
            userPreferencesRepository.setDontAskDelete(true)
        }
    }

    suspend fun getDontAskDeleteOnce() = userPreferencesRepository
        .getDontAskDelete()
        .first()

    fun updateLastQuestion() {
        _uiState.update { state ->
            state.copy(
                qAType = QAType.QUESTION,
                isLastQuestion = false,
                contadorPregunta = state.contadorPregunta + 1
            )
        }
    }
}