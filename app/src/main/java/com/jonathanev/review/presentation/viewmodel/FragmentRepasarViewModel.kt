package com.jonathanev.review.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.domain.GenerateTextColorRangesUseCase
import com.jonathanev.review.presentation.model.EstadoUI
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.model.TypeContent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.domain.GetContentItemsUseCase
import com.jonathanev.review.domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.domain.repository.PathProvider
import com.jonathanev.review.presentation.mapper.toDomain
import com.jonathanev.review.presentation.mapper.toUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentRepasarViewModel @Inject constructor(
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getContentItemsUseCase: GetContentItemsUseCase,
    private val pathProvider: PathProvider,
    private val generateTextColorRangesUseCase: GenerateTextColorRangesUseCase
) : ViewModel() {
    private var _preguntas: MutableList<QuestionItemUi> = mutableListOf()
    val preguntas: List<QuestionItemUi> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemUi> = mutableListOf()
    val respuestas: List<QuestionItemUi> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private val _uiState = MutableStateFlow(EstadoUI())
    val uiState = _uiState.asStateFlow()

    private val _typeContent = MutableLiveData(TypeContent.QUESTION)
    val typeContent get() = _typeContent

    private val _eventsMessages = MutableSharedFlow<UIStopEvent>()
    val eventsMessages = _eventsMessages.asSharedFlow()

    private fun addOneCount() {
        _contadorPregunta++
    }

    private fun minusOneCount() {
        _contadorPregunta--
    }

    private fun getCount() = contadorPregunta

    private fun setCountZero() {
        _contadorPregunta = 0
    }

    private fun setContadorPregunta(positionContent: Int) {
        _contadorPregunta = positionContent
    }

    fun restartReview() {
        setCountZero()
        resetContentLists()
        showContents()
    }

    fun getObtenerDatosXML(positionContent: Int) {
        setContadorPregunta(positionContent)

        if (respuestas.isEmpty()) {
            //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
            val datos = getObtenerDatosXMLUseCase.invoke()

            val tempQuestions =
                datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toList()
            val tempAnswers =
                datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toList()

            val questionsDomain = generateTextColorRangesUseCase.invoke(tempQuestions)
            val answersDomain = generateTextColorRangesUseCase.invoke(tempAnswers)
            _preguntas = questionsDomain.map { it.toUi() }.toMutableList()
            _respuestas = answersDomain.map { it.toUi() }.toMutableList()
        }
    }

    fun swapTypeContent() {
        _typeContent.value =
            if (typeContent.value == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION

        resetContentLists()
        showContents()
    }

    fun nextQuestion() {
        val count = getCount()
        val totalQuestions = respuestas.size - 1

        if (count == totalQuestions) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.RestartGuide("Se acabaron las preguntas, ¿Quieres repetir la guía?")
                )
            }
            return
        }

        addOneCount()
        setQuestionInTypeContent()
        resetContentLists()
        showContents()
    }

    fun beforeQuestion() {
        val count = getCount()

        if (count == 0) {
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.NotQuestionBefore("Ya no tienes preguntas anteriores")
                )
            }
            return
        }

        resetContentLists()
        minusOneCount()
        setQuestionInTypeContent()
        showContents()
    }

    private fun setQuestionInTypeContent() {
        _typeContent.value = TypeContent.QUESTION
    }

    private fun resetContentLists() = _uiState.update { state ->
        state.copy(
            imageList = emptyList(),
            textList = emptyList()
        )
    }

    private fun showContents() {
        val contentList =
            if (typeContent.value == TypeContent.QUESTION) preguntas else respuestas

        if (contentList.isNotEmpty()) {
            val contentListDomain = contentList.map { it.toDomain() }
            val responseContent = getContentItemsUseCase.invoke(contentListDomain, contadorPregunta)

            val listTextUi = responseContent.first.map { it.toUi() }
            val listImageUi = responseContent.second.map { it.toUi() }

            _uiState.update { state ->
                state.copy(
                    textList = listTextUi,
                    imageList = listImageUi
                )
            }
        }
    }

    private fun getCurrentPath() = pathProvider.getCurrentPath()
}