package com.jonathanev.review.presentation.viewmodel

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetActiveGuideUseCase
import com.jonathanev.review.domain.GetGuideContextUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.SaveTempImageUseCase
import com.jonathanev.review.domain.SetContentUseCase
import com.jonathanev.review.domain.SetContextEditUseCase
import com.jonathanev.review.domain.SetCrearXmlUseCase
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
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.model.SaveGuideMode
import com.jonathanev.review.presentation.state.GuideScreenUiState
import com.jonathanev.review.ui.model.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jonathanev.review.ui.screens.toAnnotatedString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jonathanev.review.ui.model.QAType as QATypeUI

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setContentUseCase: SetContentUseCase,
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val setCrearXmlUseCase: SetCrearXmlUseCase,
    private val getActiveGuideUseCase: GetActiveGuideUseCase,
    private val getGuideContextUseCase: GetGuideContextUseCase,
    private val setContextEditUseCase: SetContextEditUseCase,
    private val saveTempImageUseCase: SaveTempImageUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private companion object {
        const val KEY_GUIDE_STATE = "key_guide_ui_state"
        const val KEY_DRAFT_TEXT = "key_draft_text"
        const val KEY_DRAFT_SELECTION_START = "key_draft_selection_start"
        const val KEY_DRAFT_SELECTION_END = "key_draft_selection_end"
    }

    // La UI reacciona directamente a los cambios en SavedStateHandle
    val uiState: StateFlow<GuideScreenUiState> = savedStateHandle.getStateFlow(
        KEY_GUIDE_STATE,
        GuideScreenUiState.Loading
    )

    private fun updateState(newState: GuideScreenUiState) {
        savedStateHandle[KEY_GUIDE_STATE] = newState
    }

    init {
        observeGuideContext()
    }

    private fun observeGuideContext() {
        viewModelScope.launch {
            // CRÍTICO: Combinamos con uiState para reaccionar cuando el estado vuelve a ser Loading (tras Discard)
            combine(
                getActiveGuideUseCase.invoke(),
                getGuideContextUseCase.invoke(),
                uiState
            ) { activeGuideDomain, moveContext, state ->
                val context = moveContext ?: activeGuideDomain?.let {
                    GuideContext.Browsing(guide = it, position = 0)
                }
                context to state
            }
                .distinctUntilChanged()
                .flowOn(Dispatchers.IO)
                .collect { (context, state) ->
                    // 1. Si no hay contexto aún, NO emitimos Error. Simplemente esperamos en Loading.
                    if (context == null) {
                        return@collect
                    }

                    // 2. Extraemos la guía y la posición solicitada
                    val (guide, targetPosition) = when (context) {
                        is GuideContext.Browsing -> context.guide to context.position
                        is GuideContext.Editing -> context.guide to context.position
                        is GuideContext.Creating -> context.guide to 0
                        else -> {
                            // Solo si el contexto es explícitamente inválido, pasamos a Error
                            if (state !is GuideScreenUiState.Error) updateState(GuideScreenUiState.Error)
                            return@collect
                        }
                    }

                    // 3. Si ya tenemos Success y el contexto coincide, NO cargamos (protege cambios en memoria)
                    if (state is GuideScreenUiState.Success && state.guideContext == context) {
                        return@collect
                    }

                    // 4. Si el contexto cambió pero el estado sigue siendo Success del contexto anterior, forzamos Loading
                    if (state is GuideScreenUiState.Success && state.guideContext != context) {
                        updateState(GuideScreenUiState.Loading)
                        return@collect // La siguiente emisión del combine manejará la carga
                    }

                    // 5. Si el estado es Loading (arranque o tras Discard), procedemos a cargar
                    if (state is GuideScreenUiState.Loading) {
                        if (context is GuideContext.Creating) {
                            updateState(
                                GuideScreenUiState.Success(
                                    fileName = guide.nameGuide,
                                    description = guide.description,
                                    preguntas = listOf(QuestionItemUi(content = emptyList())),
                                    respuestas = listOf(QuestionItemUi(content = emptyList())),
                                    guideContext = context
                                )
                            )
                        } else {
                            // Carga desde XML
                            when (val result = getGuideXmlDataUseCase.invoke(context = context)) {
                                is GetGuideResult.Success -> {
                                    val questions = result.list.map { it.question.toUi() }
                                    val answers = result.list.map { it.answer.toUi() }

                                    val isAddingAtEnd = targetPosition >= questions.size

                                    val finalQuestions =
                                        if (isAddingAtEnd) questions + QuestionItemUi(content = emptyList()) else questions
                                    val finalAnswers =
                                        if (isAddingAtEnd) answers + QuestionItemUi(content = emptyList()) else answers

                                    val initialContador =
                                        if (isAddingAtEnd) questions.size else targetPosition.coerceIn(
                                            0,
                                            questions.lastIndex.coerceAtLeast(0)
                                        )

                                    updateState(
                                        GuideScreenUiState.Success(
                                            fileName = guide.nameGuide,
                                            description = guide.description,
                                            preguntas = finalQuestions.ifEmpty {
                                                listOf(
                                                    QuestionItemUi(content = emptyList())
                                                )
                                            },
                                            respuestas = finalAnswers.ifEmpty {
                                                listOf(
                                                    QuestionItemUi(content = emptyList())
                                                )
                                            },
                                            contadorPregunta = initialContador,
                                            guideContext = context,
                                            originalQuestions = finalQuestions,
                                            originalAnswers = finalAnswers
                                        )
                                    )
                                }

                                else -> updateState(GuideScreenUiState.Error)
                            }
                        }
                    }
                }
        }
    }

    private val _createGuideEvent = MutableSharedFlow<CreateGuideEvent>()
    val createGuideEvent = _createGuideEvent.asSharedFlow()

    private val _updateItemTrigger = MutableSharedFlow<Unit>()
    val updateItemTrigger = _updateItemTrigger.asSharedFlow()

    val imageList: StateFlow<List<QuestionContentUi.Image>> = uiState
        .map { state ->
            if (state is GuideScreenUiState.Success) {
                val currentSource =
                    if (state.qAType == QATypeUI.QUESTION) state.preguntas else state.respuestas
                val itemActual = currentSource.getOrNull(state.contadorPregunta)
                itemActual?.content?.filterIsInstance<QuestionContentUi.Image>() ?: emptyList()
            } else {
                emptyList()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val textList: StateFlow<List<QuestionContentUi.Text>> = uiState
        .map { state ->
            if (state is GuideScreenUiState.Success) {
                val currentSource =
                    if (state.qAType == QATypeUI.QUESTION) state.preguntas else state.respuestas
                val itemActual = currentSource.getOrNull(state.contadorPregunta)
                itemActual?.content?.filterIsInstance<QuestionContentUi.Text>() ?: emptyList()
            } else {
                emptyList()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val draftTextValue: StateFlow<TextFieldValue?> = combine(
        savedStateHandle.getStateFlow<QuestionContentUi.Text?>(KEY_DRAFT_TEXT, null),
        savedStateHandle.getStateFlow(KEY_DRAFT_SELECTION_START, 0),
        savedStateHandle.getStateFlow(KEY_DRAFT_SELECTION_END, 0)
    ) { textUi, start, end ->
        textUi?.toAnnotatedString()?.let { 
            TextFieldValue(
                annotatedString = it,
                selection = TextRange(start, end)
            ) 
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    fun initTextDraft(initialContent: QuestionContentUi.Text, isEditing: Boolean) {
        if (savedStateHandle.get<QuestionContentUi.Text?>(KEY_DRAFT_TEXT) == null) {
            val contentToSet = if (isEditing) initialContent else QuestionContentUi.Text("", emptyList())
            savedStateHandle[KEY_DRAFT_TEXT] = contentToSet
            savedStateHandle[KEY_DRAFT_SELECTION_START] = contentToSet.text.length
            savedStateHandle[KEY_DRAFT_SELECTION_END] = contentToSet.text.length
        }
    }

    fun onDraftTextChange(newValue: TextFieldValue) {
        val currentDraft = savedStateHandle.get<QuestionContentUi.Text?>(KEY_DRAFT_TEXT)
        val updatedDraft = QuestionContentUi.Text(
            text = newValue.text,
            colorRanges = newValue.annotatedString.spanStyles.mapNotNull { span ->
                if (span.item.color != Color.Unspecified) {
                    ColorRangeUi(span.start, span.end, span.item.color.toArgb())
                } else null
            }
        )
        if (currentDraft != updatedDraft) {
            savedStateHandle[KEY_DRAFT_TEXT] = updatedDraft
        }
        savedStateHandle[KEY_DRAFT_SELECTION_START] = newValue.selection.start
        savedStateHandle[KEY_DRAFT_SELECTION_END] = newValue.selection.end
    }

    fun clearTextDraft() {
        savedStateHandle[KEY_DRAFT_TEXT] = null
        savedStateHandle[KEY_DRAFT_SELECTION_START] = 0
        savedStateHandle[KEY_DRAFT_SELECTION_END] = 0
    }

    fun retryLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            val guide = getActiveGuideUseCase.invoke().firstOrNull()
            if (guide == null) {
                updateState(GuideScreenUiState.Error)
                return@launch
            }
            fetchAndEmitGuideXml(guide)
        }
    }

    private suspend fun fetchAndEmitGuideXml(guide: GuideDomainModel) {
        updateState(GuideScreenUiState.Loading)
        val context = GuideContext.Browsing(guide = guide, position = 0)

        when (val result = getGuideXmlDataUseCase.invoke(context = context)) {
            is GetGuideResult.Success -> {
                val questions = result.list.map { it.question.toUi() }
                val answers = result.list.map { it.answer.toUi() }

                updateState(
                    GuideScreenUiState.Success(
                        fileName = guide.nameGuide,
                        description = guide.description,
                        preguntas = questions.ifEmpty { listOf(QuestionItemUi(content = emptyList())) },
                        respuestas = answers.ifEmpty { listOf(QuestionItemUi(content = emptyList())) },
                        guideContext = context
                    )
                )
            }

            else -> updateState(GuideScreenUiState.Error)
        }
    }

    fun updatePosContent(currentPos: Int) {
        updateSuccessState { state ->
            state.copy(contadorContenido = currentPos)
        }
    }

    private inline fun updateSuccessState(crossinline transform: (GuideScreenUiState.Success) -> GuideScreenUiState.Success) {
        val currentState = uiState.value
        if (currentState is GuideScreenUiState.Success) {
            updateState(transform(currentState))
        }
    }

    fun addTextContent(
        textWithLabels: String,
        listSpans: List<ColorRangeUi>,
        questionContentMode: QuestionContentMode
    ) {
        val newContent = QuestionContentUi.Text(textWithLabels, listSpans)

        updateSuccessState { state ->
            val currentPosContent = when (questionContentMode) {
                QuestionContentMode.CREATING -> state.contadorContenido + 1
                QuestionContentMode.EDITING -> state.contadorContenido
            }
            val isQuestion = state.qAType == QATypeUI.QUESTION
            val sourceListUi = if (isQuestion) state.preguntas else state.respuestas
            val sourceListDomain = sourceListUi.map { it.toDomain() }

            val updatedDomainList = setContentUseCase.invoke(
                newContent = newContent.toDomain(),
                sourceList = sourceListDomain,
                contadorPregunta = state.contadorPregunta,
                contadorContenido = currentPosContent,
                isEditingMode = questionContentMode == QuestionContentMode.EDITING,
                filterType = QuestionContentDomain.Text::class.java
            )

            val updatedList = updatedDomainList.map { it.toUi() }

            state.copy(
                preguntas = if (isQuestion) updatedList else state.preguntas,
                respuestas = if (!isQuestion) updatedList else state.respuestas,
                contadorContenido = currentPosContent
            )
        }

        viewModelScope.launch {
            _updateItemTrigger.emit(Unit)
        }
        clearTextDraft()
    }

    fun addImageContent(uri: String, questionContentMode: QuestionContentMode) {
        viewModelScope.launch {
            val tempUri = saveTempImageUseCase(uri)
            val newContent = QuestionContentUi.Image(uri = tempUri, nameFile = "")

            updateSuccessState { state ->
                val currentPosContent = when (questionContentMode) {
                    QuestionContentMode.CREATING -> state.contadorContenido + 1
                    QuestionContentMode.EDITING -> state.contadorContenido
                }
                val isQuestion = state.qAType == QATypeUI.QUESTION
                val sourceListUi = if (isQuestion) state.preguntas else state.respuestas
                val currentQuestionUi = sourceListUi.getOrNull(state.contadorPregunta)

                val updatedList: List<QuestionItemUi> = if (currentQuestionUi == null) {
                    sourceListUi + QuestionItemUi(content = listOf(newContent))
                } else {
                    val sourceListDomain = sourceListUi.map { it.toDomain() }

                    val updatedDomainList = setContentUseCase.invoke(
                        newContent = newContent.toDomain(),
                        sourceList = sourceListDomain,
                        contadorPregunta = state.contadorPregunta,
                        contadorContenido = currentPosContent,
                        isEditingMode = questionContentMode == QuestionContentMode.EDITING,
                        filterType = QuestionContentDomain.Image::class.java
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
    }

    fun deleteImage(position: Int) {
        updateSuccessState { state ->
            val isQuestion = state.qAType == QATypeUI.QUESTION
            val sourceListUi = if (isQuestion) state.preguntas else state.respuestas

            val sourceListToDomain = sourceListUi.map { it.toDomain() }
            val updatedListDomain = deleteFilteredContent(
                sourceList = sourceListToDomain,
                contadorPregunta = state.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContentDomain.Image::class.java
            )
            val updatedListToUi = updatedListDomain.map { it.toUi() }
            state.copy(
                preguntas = if (isQuestion) updatedListToUi else state.preguntas,
                respuestas = if (!isQuestion) updatedListToUi else state.respuestas,
                contadorContenido = position - 1
            )
        }
    }

    fun deleteText(position: Int) {
        updateSuccessState { state ->
            val isQuestion = state.qAType == QATypeUI.QUESTION
            val sourceListUi = if (isQuestion) state.preguntas else state.respuestas

            val sourceListToDomain = sourceListUi.map { it.toDomain() }
            val updatedListDomain = deleteFilteredContent(
                sourceList = sourceListToDomain,
                contadorPregunta = state.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContentDomain.Text::class.java
            )
            val updatedListToUi = updatedListDomain.map { it.toUi() }

            state.copy(
                preguntas = if (isQuestion) updatedListToUi else state.preguntas,
                respuestas = if (!isQuestion) updatedListToUi else state.respuestas,
                contadorContenido = position - 1
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
                val targetContent = item.content
                    .filter { filterType.isInstance(it) }
                    .getOrNull(posFiltered)

                val newContentList = if (targetContent != null) {
                    item.content.filter { it !== targetContent }
                } else {
                    item.content
                }
                item.copy(content = newContentList)
            } else {
                item
            }
        }
    }


    fun onCardTypeChanged(cardTypeClicked: QATypeUI) {
        updateSuccessState { state ->
            state.copy(qAType = cardTypeClicked)
        }
    }

    fun previousQuestion() {
        updateSuccessState { state ->
            val nuevoContadorPregunta = state.contadorPregunta - 1
            val preguntaDestino = state.preguntas.getOrNull(nuevoContadorPregunta)
            val tieneContenido = preguntaDestino?.content?.isNotEmpty() == true
            val nuevoContadorContenido = if (tieneContenido) 0 else -1

            state.copy(
                contadorPregunta = nuevoContadorPregunta,
                qAType = QATypeUI.QUESTION,
                contadorContenido = nuevoContadorContenido
            )
        }
    }

    private fun sendNotification(event: CreateGuideEvent) {
        viewModelScope.launch {
            _createGuideEvent.emit(event)
        }
    }

    fun nextQuestion() {
        updateSuccessState { state ->
            val nuevoContadorPregunta = state.contadorPregunta + 1
            val preguntaDestino = state.preguntas.getOrNull(nuevoContadorPregunta)
            val tieneContenido = preguntaDestino?.content?.isNotEmpty() == true
            val nuevoContadorContenido = if (tieneContenido) 0 else -1

            state.copy(
                contadorPregunta = nuevoContadorPregunta,
                qAType = QATypeUI.QUESTION,
                contadorContenido = nuevoContadorContenido
            )
        }
    }

    private fun appendEmptyQuestion(targetIndex: Int) {
        updateSuccessState { state ->
            val safeIndex = targetIndex.coerceIn(0, state.preguntas.size)
            val updatedPreguntas = state.preguntas.toMutableList().apply {
                add(safeIndex, QuestionItemDomain(content = emptyList()).toUi())
            }
            val updatedRespuestas = state.respuestas.toMutableList().apply {
                add(safeIndex, QuestionItemDomain(content = emptyList()).toUi())
            }

            state.copy(
                preguntas = updatedPreguntas,
                respuestas = updatedRespuestas,
                qAType = QATypeUI.QUESTION,
                mediaSelected = ContentType.TEXT,
                contadorPregunta = safeIndex,
                contadorContenido = -1
            )
        }
    }

    fun addNextQuestion() {
        val currentIndex = (uiState.value as? GuideScreenUiState.Success)?.contadorPregunta ?: 0
        appendEmptyQuestion(targetIndex = currentIndex + 1)
    }

    fun addQuestionAtEnd() {
        val totalItems = (uiState.value as? GuideScreenUiState.Success)?.preguntas?.size ?: 0
        appendEmptyQuestion(targetIndex = totalItems)
    }

    fun saveGuide() {
        clearTextDraft()
        viewModelScope.launch {
            val currentState = uiState.value as? GuideScreenUiState.Success ?: return@launch

            val (guideDomainModel, saveGuideMode) = when (val currentContext =
                currentState.guideContext) {
                is GuideContext.Creating -> currentContext.guide to SaveGuideMode.Create
                is GuideContext.Editing -> currentContext.guide to SaveGuideMode.Update
                else -> {
                    sendNotification(ErrorGuideCreated("Error al guardar la guia"))
                    return@launch
                }
            }

            val response = setCrearXmlUseCase.invoke(
                guideDomainModel = guideDomainModel,
                preguntas = currentState.preguntas.map { it.toDomain() },
                respuestas = currentState.respuestas.map { it.toDomain() },
                saveGuideMode = saveGuideMode.toDomain()
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
                    sendNotification(SuccessGuideCreated("Guia guardada satisfactoriamente"))
            }
        }
    }

    fun onCloseGuide() {
        sendNotification(CreateGuideEvent.CloseGuide)
    }

    fun deleteQuesAns() {
        updateSuccessState { state ->
            val total = state.preguntas.size
            val index = state.contadorPregunta

            if (total <= 1) {
                state.copy(
                    preguntas = listOf(QuestionItemUi(content = emptyList())),
                    respuestas = listOf(QuestionItemUi(content = emptyList())),
                    contadorPregunta = 0,
                    contadorContenido = -1,
                    qAType = QATypeUI.QUESTION,
                    mediaSelected = ContentType.TEXT,
                    showDialogDeleteQuestion = false,
                    showDialogRepeatGuide = false
                )
            } else {
                val newPreguntas = state.preguntas.filterIndexed { i, _ -> i != index }
                val newRespuestas = state.respuestas.filterIndexed { i, _ -> i != index }
                val newIndex = if (index > 0) index - 1 else 0

                val preguntaDestino = state.preguntas.getOrNull(newIndex)
                val tieneContenido = preguntaDestino?.content?.isNotEmpty() == true
                val nuevoContadorContenido = if (tieneContenido) 0 else -1

                state.copy(
                    preguntas = newPreguntas,
                    respuestas = newRespuestas,
                    contadorPregunta = newIndex,
                    contadorContenido = nuevoContadorContenido,
                )
            }
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

    fun onDeleteQuestionRequested() {
        viewModelScope.launch {
            val dontAskQuestion = getDontAskDeleteOnce()
            if (dontAskQuestion) {
                deleteQuesAns()
                sendNotification(CreateGuideEvent.QADeleted)
            } else {
                updateSuccessState { state ->
                    state.copy(showDialogDeleteQuestion = true)
                }
            }
        }
    }

    fun onConfirmDeleteQuestion(dontAskAgain: Boolean) {
        updateSuccessState { state ->
            state.copy(showDialogDeleteQuestion = false)
        }
        if (dontAskAgain) {
            saveDontAskDelete()
        }
        deleteQuesAns()
        sendNotification(CreateGuideEvent.QADeleted)
    }

    fun onDismissDialogDeleteQuestion() {
        updateSuccessState { state ->
            state.copy(showDialogDeleteQuestion = false)
        }
    }

    fun onDismissDialogRepeatGuide() {
        updateSuccessState { state ->
            state.copy(showDialogRepeatGuide = false)
        }
    }

    fun onDismissDialogSelectColor() {
        updateSuccessState { state ->
            state.copy(showDialogColor = false)
        }
    }

    fun showDialogSelectColor() {
        updateSuccessState { state ->
            state.copy(showDialogColor = true)
        }
    }

    fun onChangeColor(actualColor: Int) {
        updateSuccessState { state ->
            state.copy(colorType = ColorType.RandomColor(actualColor))
        }
    }

    fun onDefaultcolor() {
        updateSuccessState { state ->
            state.copy(
                colorType = ColorType.Default,
                showDialogColor = false
            )
        }
    }

    fun onNextQuestionRequested(actualQuestion: Int, totalQuestions: Int) {
        if (actualQuestion == totalQuestions) {
            updateSuccessState { state ->
                state.copy(showDialogRepeatGuide = true)
            }
        } else {
            nextQuestion()
        }
    }

    fun onFilterTypeChanged(filterTypeClicked: ContentType) {
        updateSuccessState { state ->
            state.copy(mediaSelected = filterTypeClicked)
        }
    }

    fun onDiscardGuide() {
        clearTextDraft()
        // Al asignar Loading, forzamos a que el collect del combine re-ejecute la lógica de carga.
        updateState(GuideScreenUiState.Loading)
    }


    fun restartGuide() {
        updateSuccessState { state ->
            state.copy(
                contadorPregunta = 0,
                qAType = QATypeUI.QUESTION,
                mediaSelected = ContentType.TEXT,
                showDialogRepeatGuide = false
            )
        }
    }

    fun onBackFromEditor() {
        updateSuccessState { it.copy(showDialogDiscardDraft = true) }
    }

    fun onConfirmDiscardDraft() {
        clearTextDraft()
        updateSuccessState { it.copy(showDialogDiscardDraft = false) }
    }

    fun onDismissDiscardDraft() {
        updateSuccessState { it.copy(showDialogDiscardDraft = false) }
    }

    fun switchToEditMode() {
        viewModelScope.launch {
            val currentState = uiState.value as? GuideScreenUiState.Success ?: return@launch
            val browsingContext =
                currentState.guideContext as? GuideContext.Browsing ?: return@launch

            val editingContext = GuideContext.Editing(
                guide = browsingContext.guide,
                position = currentState.contadorPregunta
            )

            setContextEditUseCase(editingContext)

            updateSuccessState { state ->
                state.copy(guideContext = editingContext)
            }
        }
    }

    fun onMoveItem(from: Int, to: Int) {
        updateSuccessState { state ->
            val isQuestion = state.qAType == QATypeUI.QUESTION
            val list = if (isQuestion) state.preguntas else state.respuestas
            val currentTarget = list.getOrNull(state.contadorPregunta)

            val fullContent = currentTarget?.content?.toMutableList()
            if (fullContent == null) {
                sendNotification(CreateGuideEvent.ErrorMoveContent)
                return@updateSuccessState state
            }

            val filteredContentIndices = fullContent.indices.filter { index ->
                fullContent[index] is QuestionContentUi.Image
            }

            if (from !in filteredContentIndices.indices || to !in filteredContentIndices.indices) {
                sendNotification(CreateGuideEvent.ErrorMoveContent)
                return@updateSuccessState state
            }

            val realFromIndex = filteredContentIndices[from]
            val realToIndex = filteredContentIndices[to]

            val itemToMove = fullContent.removeAt(realFromIndex)
            fullContent.add(realToIndex, itemToMove)

            val updatedTarget = currentTarget.copy(content = fullContent)
            val updatedList = list.toMutableList().apply {
                set(state.contadorPregunta, updatedTarget)
            }

            if (isQuestion) {
                state.copy(
                    preguntas = updatedList,
                    contadorContenido = to
                )
            } else {
                state.copy(
                    respuestas = updatedList,
                    contadorContenido = to
                )
            }
        }
    }

    fun isEditingOrCreating(): Boolean {
        val state = uiState.value
        if (state is GuideScreenUiState.Success) {
            return state.guideContext is GuideContext.Creating ||
                    state.guideContext is GuideContext.Editing
        }
        return false
    }

    fun hasChangesInGuide(): Boolean {
        val state = uiState.value as? GuideScreenUiState.Success ?: return false
        
        return when (state.guideContext) {
            is GuideContext.Creating -> {
                // Hay cambios si hay más de una pregunta o si la primera tiene algo
                state.preguntas.size > 1 || 
                state.preguntas.firstOrNull()?.content?.isNotEmpty() == true ||
                state.respuestas.firstOrNull()?.content?.isNotEmpty() == true
            }
            is GuideContext.Editing -> {
                // Comparamos con la lista original cargada
                val currentQuestions = state.preguntas
                val currentAnswers = state.respuestas
                val originalQuestions = state.originalQuestions
                val originalAnswers = state.originalAnswers

                (originalQuestions != null && currentQuestions != originalQuestions) ||
                (originalAnswers != null && currentAnswers != originalAnswers)
            }
            else -> false
        }
    }
}
