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
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.UserPreferencesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.SaveGuideErrors
import com.jonathanev.review.domain.result.UpdateGuideResult
import com.jonathanev.review.presentation.event.CreateGuideEvent
import com.jonathanev.review.presentation.event.CreateGuideEvent.ErrorGuideCreated
import com.jonathanev.review.presentation.event.CreateGuideEvent.SuccessGuideCreated
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.model.RelativeGuidePath
import com.jonathanev.review.presentation.model.SaveGuideMode
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
import com.jonathanev.review.ui.model.QAType as QATypeUI

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setContentUseCase: SetContentUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val getSaveGuidesUseCase: GetSaveGuidesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val guideQuestionExtractor: GuideQuestionExtractor,
    private val setCrearXmlUseCase: SetCrearXmlUseCase
) : ViewModel() {
    private var isInitialized = false
    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

    private val _createGuideEvent = MutableSharedFlow<CreateGuideEvent>()
    val createGuideEvent = _createGuideEvent.asSharedFlow()

    val imageList: StateFlow<List<QuestionContentUi.Image>> = _uiState
        .map { state ->
            val currentSource =
                if (state.qAType == QATypeUI.QUESTION) state.preguntas else state.respuestas
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
                if (state.qAType == QATypeUI.QUESTION) state.preguntas else state.respuestas
            val itemActual = currentSource.getOrNull(state.contadorPregunta)
            val contenidosText =
                itemActual?.content?.filterIsInstance<QuestionContentUi.Text>() ?: emptyList()

            contenidosText
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
                val fotos = state.preguntas.getOrNull(state.contadorPregunta)?.content?.filterIsInstance<QuestionContentUi.Text>()?.size ?: 0

                // Imprimimos la pila de llamadas para saber QUIÉN hizo el .update o .value
                val stackTrace = Exception("REVISION_ESTADO").stackTraceToString()

                Log.d("DEBUG_INTERNO", "EL ESTADO MAESTRO CAMBIÓ: Preguntas size = ${state.preguntas.size}, Fotos = $fotos\nOrigen:\n$stackTrace")
            }
        }
    }*/

    fun initUIState() {
        _uiState.value = GuideUiState()
    }

    fun loadInitialData(guideMode: GuideMode, relativeGuidePath: RelativeGuidePath) {
        if (isInitialized) return
        isInitialized = true

        when (guideMode) {
            is GuideMode.Create -> initUIState()
            is GuideMode.Edit -> {
                getObtenerDatosXML(
                    posQuestion = guideMode.posQuestion,
                    nameGuide = guideMode.nameGuide,
                    relativeGuidePath = relativeGuidePath
                )
            }

            is GuideMode.Review -> {
                getObtenerDatosXML(
                    posQuestion = guideMode.posQuestion,
                    nameGuide = guideMode.nameGuide,
                    relativeGuidePath = relativeGuidePath
                )
            }
        }
    }

    fun setEditingMode(value: Boolean, position: Int) {
        _uiState.update {
            it.copy(
                isEditing = value,
                contadorContenido = position
            )
        }
    }

    fun updatePosContent(currentPos: Int) {
        _uiState.update { state ->
            state.copy(
                contadorContenido = currentPos
            )
        }
    }

    fun addTextContent(
        textWithLabels: String,
        listSpans: List<ColorRangeUi>,
        questionContentMode: QuestionContentMode
    ) {
        val newContent = QuestionContentUi.Text(textWithLabels, listSpans)

        _uiState.update { state ->
            val currentPosContent = when (questionContentMode) {
                QuestionContentMode.CREATING -> state.contadorContenido + 1
                QuestionContentMode.EDITING -> state.contadorContenido
            }
            val isQuestion = state.qAType == QATypeUI.QUESTION
            val sourceListUi = if (isQuestion) state.preguntas else state.respuestas

            val currentQuestionUi = sourceListUi.getOrNull(state.contadorPregunta)

            // Agregar una pregunta + contenido
            val updatedList: List<QuestionItemUi> = if (currentQuestionUi == null) {
                sourceListUi + QuestionItemUi(content = listOf(newContent))
            } else { // Agregar o editar contenido
                val sourceListDomain = sourceListUi.map { it.toDomain() }

                val updatedDomainList = setContentUseCase.invoke(
                    newContent = newContent.toDomain(),
                    sourceList = sourceListDomain,
                    contadorPregunta = state.contadorPregunta,
                    contadorContenido = currentPosContent,
                    isEditingMode = questionContentMode == QuestionContentMode.EDITING,
                    filterType = QuestionContentDomain.Text::class.java
                )

                updatedDomainList.map { it.toUi() }
            }

            state.copy(
                preguntas = if (isQuestion) updatedList else state.preguntas,
                respuestas = if (!isQuestion) updatedList else state.respuestas,
                contadorContenido = currentPosContent
            )
        }
    }

    fun addImageContent(questionContentMode: QuestionContentMode) {
        _uiState.update { state ->
            val currentPosContent =
                if (questionContentMode == QuestionContentMode.CREATING) {
                    uiState.value.contadorContenido + 1
                } else {
                    uiState.value.contadorContenido
                }
            val uriAAgregar = state.actualUri ?: return@update state

            val isQuestion = state.qAType == QATypeUI.QUESTION
            val sourceListUi = if (isQuestion) state.preguntas else state.respuestas
            val currentQuestionUi = sourceListUi.getOrNull(state.contadorPregunta)

            val newContent = QuestionContentDomain.Image(uri = uriAAgregar, nameFile = "")

            // Agregar una pregunta + contenido
            val updatedList: List<QuestionItemUi> = if (currentQuestionUi == null) {
                sourceListUi + QuestionItemDomain(content = listOf(newContent)).toUi()
            } else { // Agregar o editar contenido
                val sourceListDomain = sourceListUi.map { it.toDomain() }

                val updatedDomainList = setContentUseCase.invoke(
                    newContent = newContent,
                    sourceList = sourceListDomain,
                    contadorPregunta = state.contadorPregunta,
                    contadorContenido = currentPosContent,
                    isEditingMode = questionContentMode == QuestionContentMode.EDITING,
                    filterType = QuestionContentDomain.Text::class.java
                )

                updatedDomainList.map { it.toUi() }
            }

            state.copy(
                preguntas = if (isQuestion) updatedList else state.preguntas,
                respuestas = if (!isQuestion) updatedList else state.respuestas,
                actualUri = null,
                contadorContenido = currentPosContent
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
            val isQuestion = currentState.qAType == QATypeUI.QUESTION
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
                contadorContenido = 0
                //contadorContenido = -1
            )
        }
    }

    fun deleteText(position: Int) {
        _uiState.update { currentState ->
            val isQuestion = currentState.qAType == QATypeUI.QUESTION
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
                contadorContenido = 0
                //contadorContenido = -1
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


    fun onCardTypeChanged(cardTypeClicked: QATypeUI) {
        _uiState.update { state ->
            state.copy(qAType = cardTypeClicked)
        }
    }

    fun previousQuestion() {
        _uiState.update { state ->
            val nuevoContador = state.contadorPregunta - 1

            state.copy(
                contadorPregunta = nuevoContador,
                qAType = QATypeUI.QUESTION,
                contadorContenido = 0
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
            _createGuideEvent.emit(CreateGuideEvent.ErrorGuideCreated(text))
        }
    }

    fun nextQuestion() {
        _uiState.update { state ->
            state.copy(
                contadorPregunta = state.contadorPregunta + 1,
                qAType = QATypeUI.QUESTION,
                contadorContenido = 0
            )
        }
    }

    fun addNextQuestion() {
        _uiState.update { state ->
            state.copy(
                contadorPregunta = state.contadorPregunta + 1
            )
        }

        addContentEmpty()
    }

    private fun addContentEmpty() {
        _uiState.update { state ->
            val pos = state.contadorPregunta

            val updatedPreguntas = state.preguntas.toMutableList().apply {
                val index = pos.coerceIn(0, size)
                add(index, QuestionItemDomain(content = emptyList()).toUi())
            }

            val updatedRespuestas = state.respuestas.toMutableList().apply {
                val index = pos.coerceIn(0, size)
                add(index, QuestionItemDomain(content = emptyList()).toUi())
            }

            state.copy(
                preguntas = updatedPreguntas,
                respuestas = updatedRespuestas
            )
        }
    }

    private fun findGuide(nameGuide: String): GuideDomainModel? =
        getSaveGuidesUseCase.invoke().find { it.nameGuide == nameGuide }

    private fun loadGuideXml(
        guide: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ): GetGuideResult =
        getGuideXmlDataUseCase.invoke(GuideContext.Editing(guide, relativeGuidePath.toDomain()))

    private fun handleGuideResult(
        result: GetGuideResult,
        noQuestion: Int
    ) {
        when (result) {
            is GetGuideResult.Success -> updateUiWithContent(result, noQuestion)
            GetGuideResult.InvalidFormat -> showMessage("La guia está dañada")
            GetGuideResult.NotFound -> showMessage("No se ha encontrado la guia")
            GetGuideResult.UnknownError -> showMessage("Error desconocido")
        }
    }


    private fun updateUiWithContent(
        result: GetGuideResult.Success,
        noQuestion: Int
    ) {
        val (questions, answers) = guideQuestionExtractor.map(result)

        _uiState.update { state ->
            state.copy(
                contadorPregunta = calculatePosition(noQuestion, answers.size),
                qAType = QATypeUI.QUESTION,
                preguntas = questions.map { it.toUi() },
                respuestas = answers.map { it.toUi() },
                isLastQuestion = if (noQuestion == -1) false else null
            )
        }
    }

    private fun calculatePosition(noQuestion: Int, totalAnswers: Int): Int =
        if (noQuestion == -1) totalAnswers else noQuestion

    fun getObtenerDatosXML(
        posQuestion: Int,
        nameGuide: String,
        relativeGuidePath: RelativeGuidePath
    ) {
        if (uiState.value.respuestas.isNotEmpty()) return

        val guide = findGuide(nameGuide) ?: run {
            showMessage("No se ha encontrado la guia a renombrar")
            return
        }

        val result = loadGuideXml(guide, relativeGuidePath)
        handleGuideResult(result, posQuestion)
    }

    private fun isDataValid(): Boolean {
        val stateUi = uiState.value
        val preguntasDomain = stateUi.preguntas.map { it.toDomain() }
        val respuestasDomain = stateUi.respuestas.map { it.toDomain() }

        val listWithMoreQuestions =
            if (preguntasDomain.size > respuestasDomain.size)
                preguntasDomain
            else
                respuestasDomain

        val questionHasContent = preguntasDomain.isEmpty()
        if (questionHasContent) {
            showMessage("Debes tener minimo algo para guardar")
            return false
        }

        listWithMoreQuestions.forEachIndexed { index, _ ->
            // Validar consistencia en la posición actual
            val currentQuestionHasText =
                preguntasDomain.getOrNull(index)?.hasText() ?: false
            val currentAnswerHasText =
                respuestasDomain.getOrNull(index)?.hasText() ?: false

            if (!currentQuestionHasText || !currentAnswerHasText) {
                sendNotification(CreateGuideEvent.WithoutTextInPos(index + 1))
                return false
            }
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
                relativeGuidePath = relativeGuidePath.toDomain(),
                mode = mode.toDomain()
            )

            when (response) {
                UpdateGuideResult.ErrorPath ->
                    sendNotification(ErrorGuideCreated("Error en la ruta inicial"))

                UpdateGuideResult.ErrorUpdateGuide ->
                    sendNotification(ErrorGuideCreated("Error al cargar datos de la guia"))

                UpdateGuideResult.ImagesFailed ->
                    sendNotification(SuccessGuideCreated("Guia guardada con imagenes corruptas"))

                is UpdateGuideResult.SaveFailed -> {
                    when (response.cause) {
                        SaveGuideErrors.CommitChangesFailed ->
                            sendNotification(ErrorGuideCreated("Error al guardar la guia"))

                        SaveGuideErrors.InsufficientStorageOrDiskError ->
                            sendNotification(ErrorGuideCreated("Error de entrada/salida al guardar la guía"))

                        SaveGuideErrors.StoragePermissionDenied ->
                            sendNotification(ErrorGuideCreated("Permisos insuficientes para guardar la guía"))
                    }
                }

                UpdateGuideResult.Success ->
                    sendNotification(
                        SuccessGuideCreated("Guia guardada satisfactoriamente")
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
            userPreferencesRepository.setDontAskDelete(true)
        }
    }

    suspend fun getDontAskDeleteOnce() = userPreferencesRepository
        .getDontAskDelete()
        .first()

    fun updateLastQuestion() {
        _uiState.update { state ->
            state.copy(
                qAType = QATypeUI.QUESTION,
                isLastQuestion = false,
                contadorPregunta = state.contadorPregunta + 1
            )
        }
    }

    /*fun restartGuide() {
        _uiState.update { state ->
            state.copy(contadorPregunta = 0)
        }

        _uiState.update { state ->
            state.copy(
                contadorPregunta = calculatePosition(noQuestion, answers.size),
                qAType = QAType.QUESTION,
                preguntas = questions.map { it.toUi() },
                respuestas = answers.map { it.toUi() },
                isLastQuestion = if (noQuestion == -1) false else null
            )
        }
    }*/
}