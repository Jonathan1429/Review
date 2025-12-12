package com.jonathanev.review.UI.ViewModel.Fragments

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.Model.ContentWrapper
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UiStopEvent
import com.jonathanev.review.Domain.GetContentItemsUseCase
import com.jonathanev.review.Domain.GetTextWithoutLabelsUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import com.jonathanev.review.Domain.SetContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setContentUseCase: SetContentUseCase,
    private val getContentItemsUseCase: GetContentItemsUseCase,
    private val getTextWithoutLabelsUseCase: GetTextWithoutLabelsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EstadoUI())
    val uiState = _uiState.asStateFlow()

    private val _uiStopEvent = MutableSharedFlow<UiStopEvent>()
    val uiStopEvent = _uiStopEvent.asSharedFlow()

    private var _preguntas = mutableListOf<QuestionItem>()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas = mutableListOf<QuestionItem>()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var contadorContenido: Int = -1
    private lateinit var actualUri: Uri
    private var _typeContent: MutableLiveData<TypeContent> = MutableLiveData(TypeContent.QUESTION)
    val typeContent: LiveData<TypeContent> get() = _typeContent

    private var isEditingMode: Boolean = false

    private fun countSubtractCount(){
        _contadorPregunta--
    }

    private fun swapTypeContent() {
        _typeContent.value =
            if (typeContent.value == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION
    }

    private fun setTypeContentWithQuestion(){
        _typeContent.value = TypeContent.QUESTION
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

    fun setColocarEtiquetas(text: String, listSpans: List<ColorRange>): String {
        return setColocarEtiquetasUseCase.invoke(text, listSpans)
    }

    fun setEditingMode(value: Boolean, position: Int) {
        isEditingMode = value
        contadorContenido = position
    }

    private fun resetEditingMode() {
        isEditingMode = false
        contadorContenido = -1
    }

    private fun resetContentLists() = _uiState.update { state ->
        state.copy(
            imageList = emptyList(),
            textList = emptyList()
        )
    }

    fun addTextContent(textWithLabels: String, listSpans: List<ColorRange>) {
        val mutablePivRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        val newContent = QuestionContent.Text(textWithLabels, listSpans)

        // Si la lista está vacía y no estamos editando, creamos un nuevo item con el contenido.
        if (mutablePivRefList.isEmpty()) {
            mutablePivRefList.add(QuestionItem(content = listOf(newContent)))
            resetEditingMode()
            resetContentLists()
            showContents()
            return
        }

        setContentUseCase.invoke(newContent, mutablePivRefList, contadorPregunta, contadorContenido, isEditingMode, QuestionContent.Text::class.java)

        resetEditingMode()
        resetContentLists()
        showContents()
    }

    fun addImageContent() {
        val mutableRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        val actualUri = getActualUri().toString()
        val encoded = setCifrarRutaImagenUseCase(actualUri, 26 - 3)
        val newContent = QuestionContent.Image(actualUri, encoded)

        // Si la lista está vacía y no estamos editando -> crear nuevo item
        if (mutableRefList.isEmpty()) {
            mutableRefList.add(QuestionItem(content = listOf(newContent)))
            resetEditingMode()
            resetContentLists()
            showContents()
            return
        }

        setContentUseCase.invoke(newContent, mutableRefList, contadorPregunta, contadorContenido, isEditingMode, QuestionContent.Image::class.java)

        resetEditingMode()
        resetContentLists()
        showContents()
    }

    private fun getActualUri() = actualUri

    fun setActualUri(uri: Uri) {
        actualUri = uri
    }

    fun deleteImage(position: Int) {
        deleteFilteredContent(position, QuestionContent.Image::class.java)

        // Refrescar UI
        resetContentLists()
        showContents()
    }

    fun deleteText(position: Int) {
        deleteFilteredContent(position, QuestionContent.Text::class.java)

        // Refrescar UI
        resetContentLists()
        showContents()
    }

    private fun deleteFilteredContent(
        posFiltered: Int,
        filterType: Class<out QuestionContent>
    ) {
        val mutableRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        val oldItem = mutableRefList[contadorPregunta]

        // 1) Envolver con índice real
        val wrappers = oldItem.content.mapIndexed { index, content ->
            ContentWrapper(index, content)
        }

        // 2) Filtrar por tipo (texto o imagen)
        val filtered = wrappers.filter { filterType.isInstance(it.content) }

        // 3) Obtener índice real
        val realIndex = filtered[posFiltered].originalIndex

        // 4) Eliminar
        val newContentList = oldItem.content.toMutableList().apply {
            removeAt(realIndex)
        }.toList()

        // 5) Guardar cambios
        mutableRefList[contadorPregunta] = oldItem.copy(content = newContentList)
    }

    fun rollPregResp() {
        val noTexts = uiState.value.textList.size
        if (noTexts == 0) {
            viewModelScope.launch {
                _uiStopEvent.emit(UiStopEvent.ShowMessage("Debes tener al menos un texto"))
            }
            return
        }

        swapTypeContent()
        resetContentLists()
        showContents()
    }

    fun previousQuestion() {
        if (contadorPregunta == 0){
            viewModelScope.launch {
                _uiStopEvent.emit(UiStopEvent.ShowMessage("Ya no hay preguntas anteriores"))
            }
            return
        }

        setTypeContentWithQuestion()
        countSubtractCount()
        resetContentLists()
        showContents()
    }
}