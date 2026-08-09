package com.jonathanev.review.presentation.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetActiveGuideUseCase
import com.jonathanev.review.domain.GetGuideContextUseCase
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.SaveTempImageUseCase
import com.jonathanev.review.domain.SetContentUseCase
import com.jonathanev.review.domain.SetCrearXmlUseCase
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
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
import com.jonathanev.review.ui.screens.toAnnotatedString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
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
    private val userPreferencesRepository: UserPreferencesRepository,
    private val setCrearXmlUseCase: SetCrearXmlUseCase,
    private val getActiveGuideUseCase: GetActiveGuideUseCase,
    private val getGuideContextUseCase: GetGuideContextUseCase,
    private val saveTempImageUseCase: SaveTempImageUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private companion object {
        const val KEY_GUIDE_STATE = "key_guide_ui_state"
    }

    private val _uiState = MutableStateFlow<GuideScreenUiState>(GuideScreenUiState.Loading)
    val uiState: StateFlow<GuideScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getActiveGuideUseCase.invoke(),
                getGuideContextUseCase.invoke()
            ) { activeGuideDomain, moveContext ->
                activeGuideDomain to moveContext
            }
                .flowOn(Dispatchers.IO)
                .collect { (activeGuideDomain, guideContext) ->
                    val context = guideContext ?: activeGuideDomain?.let {
                        GuideContext.Browsing(guide = it, position = 0)
                    }

                    if (context == null) {
                        _uiState.value = GuideScreenUiState.Loading
                        return@collect
                    }

                    // 1. SI YA EXISTE UN ESTADO EN SavedStateHandle, SE RESPETA Y NO SE REEVALÚA EL XML
                    val restoredState =
                        savedStateHandle.get<GuideScreenUiState.Success>(KEY_GUIDE_STATE)
                    if (restoredState != null && restoredState.guideContext == context) {
                        _uiState.value = restoredState
                        return@collect
                    }

                    // 2. SI YA HAY UN SUCCESS EN MEMORIA (Y CAMBIÓ ALGO EN EL FLOW), NO REESCRÍBIMALO
                    if (_uiState.value is GuideScreenUiState.Success) {
                        return@collect
                    }

                    val guide = when (context) {
                        is GuideContext.Browsing -> context.guide
                        is GuideContext.Creating -> context.guide
                        is GuideContext.Editing -> context.guide
                        else -> return@collect
                    }

                    _uiState.value = GuideScreenUiState.Loading

                    when (val result = getGuideXmlDataUseCase.invoke(context = context)) {
                        is GetGuideResult.Success -> {
                            val questions = result.list.map { it.question.toUi() }
                            val answers = result.list.map { it.answer.toUi() }

                            val newState = GuideScreenUiState.Success(
                                fileName = guide.nameGuide,
                                description = guide.description,
                                preguntas = questions.ifEmpty { listOf(QuestionItemUi(content = emptyList())) },
                                respuestas = answers.ifEmpty { listOf(QuestionItemUi(content = emptyList())) },
                                guideContext = context
                            )

                            // CARGA INICIAL: Se asigna tanto en memoria como en SavedStateHandle
                            _uiState.value = newState
                            savedStateHandle[KEY_GUIDE_STATE] = newState
                        }

                        else -> _uiState.value = GuideScreenUiState.Error
                    }
                }
        }
    }

    private val _createGuideEvent = MutableSharedFlow<CreateGuideEvent>()
    val createGuideEvent = _createGuideEvent.asSharedFlow()

    private val _updateItemTriger = MutableSharedFlow<Unit>()
    val updateItemTriger = _updateItemTriger.asSharedFlow()

    val imageList: StateFlow<List<QuestionContentUi.Image>> = uiState
        .map { state ->
            if (state is GuideScreenUiState.Success) {
                val currentSource =
                    if (state.qAType == QATypeUI.QUESTION) state.preguntas else state.respuestas
                val itemActual = currentSource.getOrNull(state.contadorPregunta)
                val contenidosImage =
                    itemActual?.content?.filterIsInstance<QuestionContentUi.Image>() ?: emptyList()

                contenidosImage
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
                val contenidosText =
                    itemActual?.content?.filterIsInstance<QuestionContentUi.Text>() ?: emptyList()

                contenidosText
            } else {
                emptyList()
            }
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

    private val _draftTextValue = MutableStateFlow<TextFieldValue?>(null)
    val draftTextValue: StateFlow<TextFieldValue?> = _draftTextValue.asStateFlow()

    fun initTextDraft(initialContent: QuestionContentUi.Text) {
        _draftTextValue.value =
            TextFieldValue(annotatedString = initialContent.toAnnotatedString())
    }

    fun onDraftTextChange(newValue: TextFieldValue) {
        _draftTextValue.value = newValue
    }

    fun clearTextDraft() {
        _draftTextValue.value = null
    }

    fun retryLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            val guide = getActiveGuideUseCase.invoke().firstOrNull()

            if (guide == null) {
                _uiState.value = GuideScreenUiState.Error
                return@launch
            }

            fetchAndEmitGuideXml(guide)
        }
    }

    private suspend fun fetchAndEmitGuideXml(guide: GuideDomainModel) {
        _uiState.value = GuideScreenUiState.Loading
        val context = GuideContext.Browsing(guide = guide, position = 0)

        when (val result = getGuideXmlDataUseCase.invoke(context = context)) {
            is GetGuideResult.Success -> {
                val questions = result.list.map { it.question.toUi() }
                val answers = result.list.map { it.answer.toUi() }

                _uiState.value = GuideScreenUiState.Success(
                    fileName = guide.nameGuide,
                    description = guide.description,
                    preguntas = questions.ifEmpty { listOf(QuestionItemUi(content = emptyList())) },
                    respuestas = answers.ifEmpty { listOf(QuestionItemUi(content = emptyList())) },
                    guideContext = context
                )
            }

            else -> _uiState.value = GuideScreenUiState.Error
        }
    }

    fun updatePosContent(currentPos: Int) {
        updateSuccessState { state ->
            state.copy(
                contadorContenido = currentPos
            )
        }
    }

    private inline fun updateSuccessState(crossinline transform: (GuideScreenUiState.Success) -> GuideScreenUiState.Success) {
        _uiState.update { state ->
            if (state is GuideScreenUiState.Success) {
                val newState = transform(state)
                savedStateHandle[KEY_GUIDE_STATE] = newState
                newState
            } else state
        }
    }

    fun addTextContent(
        textWithLabels: String,
        listSpans: List<ColorRangeUi>,
        questionContentMode: QuestionContentMode
    ) {
        val newContent = QuestionContentUi.Text(textWithLabels, listSpans)

        // Ejecución directa y atómica sobre el estado actual
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
            _updateItemTriger.emit(Unit)
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

            // 1. Calculamos la lista actualizada usando la lógica funcional de borrado
            val sourceListToDomain = sourceListUi.map { it.toDomain() }
            val updatedListDomain = deleteFilteredContent(
                sourceList = sourceListToDomain,
                contadorPregunta = state.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContentDomain.Image::class.java
            )
            val updatedListToUi = updatedListDomain.map { it.toUi() }
            // 3. Emitimos el nuevo estado con todas las limpiezas integradas
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

            // 1. Obtenemos la lista actualizada usando la función de borrado funcional
            val sourceListToDomain = sourceListUi.map { it.toDomain() }

            val updatedListDomain = deleteFilteredContent(
                sourceList = sourceListToDomain,
                contadorPregunta = state.contadorPregunta,
                posFiltered = position,
                filterType = QuestionContentDomain.Text::class.java
            )
            val updatedListToUi = updatedListDomain.map { it.toUi() }

            // 3. Emitimos el nuevo estado completo
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

    fun addNextQuestion() {
        updateSuccessState { state ->
            val targetIndex = state.contadorPregunta + 1

            val updatedPreguntas = state.preguntas.toMutableList().apply {
                add(targetIndex, QuestionItemDomain(content = emptyList()).toUi())
            }

            val updatedRespuestas = state.respuestas.toMutableList().apply {
                add(targetIndex, QuestionItemDomain(content = emptyList()).toUi())
            }

            state.copy(
                preguntas = updatedPreguntas,
                respuestas = updatedRespuestas,
                qAType = QATypeUI.QUESTION,
                mediaSelected = ContentType.TEXT,
                contadorPregunta = targetIndex,
                contadorContenido = -1
            )
        }

        //addContentEmpty()
    }

    private fun addContentEmpty() {
        updateSuccessState { state ->
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

    fun saveGuide() {
        /*if (!isDataValid()) {
            return
        }*/

        viewModelScope.launch {
            val currentState = uiState.value as? GuideScreenUiState.Success ?: return@launch

            val currentContext = getGuideContextUseCase.invoke().firstOrNull()
                ?: GuideContext.Browsing(
                    guide = GuideDomainModel(GuideVersion.V2, "", ""),
                    position = 0
                )

            val (guideDomainModel, saveGuideMode) = when (currentContext) {
                is GuideContext.Creating -> {
                    currentContext.guide to SaveGuideMode.Create
                }

                is GuideContext.Editing -> {
                    currentContext.guide to SaveGuideMode.Update
                }

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
                    sendNotification(
                        SuccessGuideCreated("Guia guardada satisfactoriamente")
                    )
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
                // Caso 1 de 1: Limpia el contenido al estado inicial por defecto
                state.copy(
                    preguntas = listOf(QuestionItemUi(content = emptyList())),
                    respuestas = listOf(QuestionItemUi(content = emptyList())),
                    contadorPregunta = 0,
                    contadorContenido = -1,
                    qAType = QATypeUI.QUESTION,
                    mediaSelected = ContentType.TEXT,
                    isLastQuestion = false,
                    showDialogDeleteQuestion = false,
                    showDialogRepeatGuide = false
                )
            } else {
                // Caso N de N: Elimina el elemento actual y ajusta el índice
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
            state.copy(
                colorType = ColorType.RandomColor(actualColor),
            )
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

    override fun onCleared() {
        super.onCleared()
        savedStateHandle.remove<GuideScreenUiState.Success>(KEY_GUIDE_STATE)
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