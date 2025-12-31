package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.presentation.model.EstadoUI
import com.jonathanev.review.presentation.state.ResponseDomain
import com.jonathanev.review.presentation.model.QuestionItemDomain
import com.jonathanev.review.Domain.model.TypeContent
import com.jonathanev.review.presentation.event.UIStopEvent
import com.jonathanev.review.Domain.GetContentItemsUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.UI.Utils.toUi
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
    private val fileRepository: FileRepository
) : ViewModel() {
    private var _preguntas: MutableList<QuestionItemDomain> = mutableListOf()
    val preguntas: MutableList<QuestionItemDomain> get() = _preguntas

    private var _respuestas: MutableList<QuestionItemDomain> = mutableListOf()
    val respuestas: MutableList<QuestionItemDomain> get() = _respuestas

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
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas =
                datos.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }.toMutableList()
            _respuestas =
                datos.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toMutableList()

            showContents()
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
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        if (contentList.isNotEmpty()) {
            val responseContent = getContentItemsUseCase.invoke(contentList, contadorPregunta)

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

    private fun getCurrentPath() = fileRepository.getCurrentPath()
}