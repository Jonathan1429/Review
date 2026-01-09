package com.jonathanev.review.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GenerateTextColorRangesUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.GetPreviewQuestionsUseCase
import com.jonathanev.review.domain.GetSaveGuidesUseCase
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.model.TypeContent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.QuestionItemUi
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
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val generateTextColorRangesUseCase: GenerateTextColorRangesUseCase,
    private val getPreviewQuestionsUseCase: GetPreviewQuestionsUseCase,
    private val getSaveGuidesUseCase: GetSaveGuidesUseCase
) : ViewModel() {
    private var cachedGuides: List<GuideDomainModel> = emptyList()

    private val _uiStatePreview = MutableStateFlow(PreviewQuestionStateUi(emptyList()))
    val uiStatePreview = _uiStatePreview.asStateFlow()

    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

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

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    fun getObtenerDatosXML(folderId: String) {
        val guideDomainModel = cachedGuides.find { it.nameGuide == folderId }
        //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
        val datos = getObtenerDatosXMLUseCase.invoke(guideDomainModel)
        //val datosProcesados = applyColorRangesToQAUseCase.invoke(datos)

        val tempQuestions =
            datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toList()
        val tempAnswers =
            datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toList()

        val questionsDomain = generateTextColorRangesUseCase.invoke(tempQuestions)
        val answersDomain = generateTextColorRangesUseCase.invoke(tempAnswers)

        initUiPreviewQuestions(datos)
        _uiState.update { currentState ->
            currentState.copy(
                preguntas = questionsDomain.map { it.toUi() },
                respuestas = answersDomain.map { it.toUi() }
            )
        }
    }

    private fun initUiPreviewQuestions(
        domainItems: List<QAItemDomain>,
    ) {
        val response = getPreviewQuestionsUseCase.invoke(domainItems)
        val responseToUi = response.map { it.toUi() }

        _uiStatePreview.value = PreviewQuestionStateUi(
            previewState = responseToUi
        )
    }

    fun initReview(uiState: GuideUiState) {
        _uiState.value = uiState
    }

    fun swapTypeContent() {
        _uiState.update { uiState ->
            uiState.copy(
                typeContent = if (uiState.typeContent == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION,
            )
        }
    }

    fun nextQuestion() {
        val state = uiState.value
        if (state.contadorPregunta == state.respuestas.size - 1) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.RestartGuide("Se acabaron las preguntas, ¿Quieres repetir la guía?")
                )
            }
            return
        }

        _uiState.update { uiState ->
            uiState.copy(
                typeContent = TypeContent.QUESTION,
                contadorPregunta = uiState.contadorPregunta + 1
            )
        }
    }

    fun beforeQuestion() {
        val count = uiState.value.contadorPregunta

        if (count == 0) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.NotQuestionBefore("Ya no tienes preguntas anteriores")
                )
            }
            return
        }

        _uiState.update { uiState ->
            uiState.copy(
                typeContent = TypeContent.QUESTION,
                contadorPregunta =  count - 1
            )
        }
    }

    fun uploadCachedGuides() {
        cachedGuides = getSaveGuidesUseCase.invoke()
    }

    private fun initUIState() {
        _uiState.value = GuideUiState()
    }

    fun restartReview() {
        _uiState.update { uiState ->
            uiState.copy(
                contadorPregunta = 0,
                contadorContenido = -1,
                typeContent = TypeContent.QUESTION
            )
        }
    }

    fun showQuestionClicked(position: Int) {
        _uiState.update { uiState ->
            uiState.copy(
                typeContent = TypeContent.QUESTION,
                contadorPregunta = position
            )
        }
    }

    fun addQuestions() {

    }
}