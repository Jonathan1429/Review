package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GetGuideXmlDataUseCase
import com.jonathanev.review.domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.domain.GetSaveGuidesUseCase
import com.jonathanev.review.domain.mapper.GuideQuestionExtractor
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.presentation.event.GuidePreviewEvent
import com.jonathanev.review.presentation.event.GuideReviewEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.state.GuideUiState
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
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
import javax.inject.Inject

@HiltViewModel
class FragmentRepasarViewModel @Inject constructor(
    private val getGuideXmlDataUseCase: GetGuideXmlDataUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    private val getSaveGuidesUseCase: GetSaveGuidesUseCase,
    private val guideQuestionExtractor: GuideQuestionExtractor
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()

    private val _uiStatePreview = MutableStateFlow(PreviewQuestionStateUi(emptyList()))
    val uiStatePreview = _uiStatePreview.asStateFlow()

    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventsMessages = MutableSharedFlow<GuideReviewEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private val _eventsPreviewMessages = MutableSharedFlow<GuidePreviewEvent>()
    val eventsPreviewMessages = _eventsPreviewMessages.asSharedFlow()

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

    fun initUiState() {
        _uiState.value = GuideUiState()
    }

    fun getObtenerDatosXML(folderId: String, relativeGuidePath: RelativeGuidePath) {
        val guideDomainModel = cachedGuides.find { it.nameGuide == folderId }
        if (guideDomainModel == null) {
            emitMessage("No se ha encontrado la guia a renombrar")
            return
        }
        //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
        when (val result = getGuideXmlDataUseCase.invoke(
            context = GuideContext.Browsing(
                guide = guideDomainModel,
                relativeGuidePath = relativeGuidePath
            )
        )) {
            is GetGuideResult.Success -> {
                val (questions, answers) = guideQuestionExtractor.map(result)
                initUiPreviewQuestions(result.list, relativeGuidePath)
                _uiState.update { currentState ->
                    currentState.copy(
                        preguntas = questions.map { it.toUi() },
                        respuestas = answers.map { it.toUi() }
                    )
                }
            }

            GetGuideResult.Error -> emitMessage("Ocurrió un error al abrir la guia")

            GetGuideResult.InvalidFormat -> emitMessage("La guia está dañada")

            GetGuideResult.NotFound -> emitMessage("No se ha encontrado la guia")

            GetGuideResult.UnknownError -> emitMessage("Error desconocido")
        }
    }

    private fun emitMessage(text: String) {
        viewModelScope.launch {
            _eventsPreviewMessages.emit(GuidePreviewEvent.ShowMessage(text))
        }
    }

    private fun initUiPreviewQuestions(
        domainItems: List<QAItemDomain>,
        relativeGuidePath: RelativeGuidePath,
    ) {
        val response = getPreviewQuestionsUseCase.invoke(domainItems, relativeGuidePath)
        val responseToUi = response.map { it.toUi() }

        _uiStatePreview.value = PreviewQuestionStateUi(
            previewState = responseToUi
        )
    }

    fun swapTypeContent() {
        _uiState.update { uiState ->
            uiState.copy(
                qAType = if (uiState.qAType == QAType.QUESTION) QAType.ANSWER else QAType.QUESTION,
            )
        }
    }

    fun nextQuestion() {
        val state = uiState.value
        if (state.contadorPregunta == state.respuestas.size - 1) {
            viewModelScope.launch {
                _eventsMessages.emit(GuideReviewEvent.RestartGuide)
            }
            return
        }

        _uiState.update { uiState ->
            uiState.copy(
                qAType = QAType.QUESTION,
                contadorPregunta = uiState.contadorPregunta + 1
            )
        }
    }

    fun beforeQuestion() {
        val count = uiState.value.contadorPregunta

        if (count == 0) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    GuideReviewEvent.NotQuestionBefore
                )
            }
            return
        }

        _uiState.update { uiState ->
            uiState.copy(
                qAType = QAType.QUESTION,
                contadorPregunta = count - 1
            )
        }
    }

    fun uploadCachedGuides() {
        cachedGuides = getSaveGuidesUseCase.invoke()
    }

    fun restartReview() {
        _uiState.update { uiState ->
            uiState.copy(
                contadorPregunta = 0,
                contadorContenido = -1,
                qAType = QAType.QUESTION
            )
        }
    }

    fun showQuestionClicked(position: Int) {
        _uiState.update { uiState ->
            uiState.copy(
                qAType = QAType.QUESTION,
                contadorPregunta = position
            )
        }
    }
}