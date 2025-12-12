package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetContentItemsUseCase
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
    private val fileRepositoryImpl: FileRepositoryImpl
) : ViewModel() {
    private var _preguntas: MutableList<QuestionItem> = mutableListOf()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas: MutableList<QuestionItem> = mutableListOf()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

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

    private fun minusOneCount(){
        _contadorPregunta--
    }

    private fun getCount() = contadorPregunta

    private fun setCountZero(){
        _contadorPregunta = 0
    }

    private fun setContadorPregunta(positionContent: Int) {
        _contadorPregunta = positionContent
    }

    fun restartReview(){
        setCountZero()
        resetContentLists()
        showContents()
    }

    fun getObtenerDatosXML(positionContent: Int) {
        setContadorPregunta(positionContent)

        if (respuestas.isEmpty()) {
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas = datos.map { it.answer }.toMutableList()

            showContents()
        }
    }

    fun swapTypeContent() {
        _typeContent.value =
            if (typeContent.value == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION

        resetContentLists()
        showContents()
    }

    fun nextQuestion(){
        val count = getCount()
        val totalQuestions = respuestas.size - 1

        if (count == totalQuestions){
            viewModelScope.launch {
                _eventsMessages.emit(
                    UIStopEvent.ShowMessage("Se acabaron las preguntas, ¿Quieres repetir la guía?")
                )
            }
            return
        }
        
        addOneCount()
        setQuestionInTypeContent()
        resetContentLists()
        showContents()
    }

    fun beforeQuestion(){
        val count = getCount()

        if (count == 0){
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

    private fun setQuestionInTypeContent(){
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

            _uiState.update { state ->
                state.copy(
                    textList = responseContent.first,
                    imageList = responseContent.second
                )
            }
        }
    }

    private fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()
}