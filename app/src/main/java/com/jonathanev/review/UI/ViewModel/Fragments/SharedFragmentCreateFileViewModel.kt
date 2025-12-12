package com.jonathanev.review.UI.ViewModel.Fragments

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.Model.ContentWrapper
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.InternalRules
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UiStopEvent
import com.jonathanev.review.Domain.GetQuestionContentsUseCase
import com.jonathanev.review.Domain.GetTextWithoutLabelsUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val getQuestionContentsUseCase: GetQuestionContentsUseCase,
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
            contentList[contadorPregunta].content.forEach { item ->
                when (item) {//val result = setPintarTextosUseCase.invoke(item, getCurrentPath())) {

                    is QuestionContent.Image -> {
                        _uiState.update { state ->
                            state.copy(
                                internalRules = InternalRules(isShowCancelar = true),
                                imageList = state.imageList + item,
                            )
                        }
                    }

                    is QuestionContent.Text -> {
                        _uiState.update { state ->
                            state.copy(
                                internalRules = InternalRules(
                                    isShowQuitColor = true,
                                    isShowSelColor = true
                                ),
                                textList = state.textList + item,
                            )
                        }
                    }

                    QuestionContent.None -> _uiState.value = EstadoUI()
                }
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

    fun addTextContent(textWithLabels: String, listSpans: List<ColorRange>) {
        val mutablePivRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        val newContent = QuestionContent.Text(textWithLabels, listSpans)

        // Si la lista está vacía y no estamos editando, creamos un nuevo item con el contenido.
        if (mutablePivRefList.isEmpty()) {
            mutablePivRefList.add(QuestionItem(content = listOf(newContent)))
            resetEditingMode()
            resetContentLists()
            updateQuestions()
            return
        }

        // Validar contadorPregunta dentro de rango
        if (contadorPregunta !in 0 until mutablePivRefList.size) return

        val originalItem = mutablePivRefList[contadorPregunta]
        val originalContent = originalItem.content.toMutableList()

        val textWrappers = originalItem.content.mapIndexedNotNull { index, item ->
            if (item is QuestionContent.Text) ContentWrapper(index, item) else null
        }

        if (isEditingMode) {
            // Validar que el wrapper exista
            if (contadorContenido !in 0 until textWrappers.size) return

            val selectedWrapper = textWrappers[contadorContenido]
            val originalIndex = selectedWrapper.originalIndex
            originalContent[originalIndex] = newContent
        } else {
            originalContent.add(newContent)
        }

        mutablePivRefList[contadorPregunta] = originalItem.copy(content = originalContent.toList())

        resetEditingMode()
        resetContentLists()
        updateQuestions()
    }

    private fun updateQuestions(shouldFlip: Boolean = false) {
        val contentList = getQuestionContentsUseCase.invoke(
            if (typeContent.value == TypeContent.QUESTION) preguntas else respuestas,
            contadorPregunta
        )

        val newImages = mutableListOf<QuestionContent.Image>()
        val newTexts = mutableListOf<QuestionContent.Text>() // usa tu modelo de texto formateado
        var internalRules = InternalRules()

        contentList.forEach { item ->
            when (item) {
                is QuestionContent.Image -> {
                    newImages.add(item)
                    internalRules = internalRules.copy(
                        isShowCancelar = true
                    )
                }

                is QuestionContent.Text -> {
                    var response = getTextWithoutLabelsUseCase.invoke(item.text)
                    response = response.copy(colorRanges = item.colorRanges)
                    newTexts.add(response)

                    internalRules = internalRules.copy(
                        isShowQuitColor = true,
                        isShowSelColor = true
                    )
                }

                QuestionContent.None -> {
                    // Reinicias todo
                    _uiState.value = EstadoUI()
                    return
                }
            }
        }

        // Solo 1 actualización de estado
        _uiState.update { state ->
            state.copy(
                shouldFlip = shouldFlip,
                internalRules = internalRules,
                imageList = state.imageList + newImages,
                textList = state.textList + newTexts
            )
        }
    }

    private fun resetContentLists() = _uiState.update { state ->
        state.copy(
            imageList = emptyList(),
            textList = emptyList()
        )
    }

    fun processImage() {
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
            updateQuestions()
            return
        }

        val oldItem = mutableRefList[contadorPregunta]
        val originalContent = oldItem.content.toMutableList()

        // Filtrar índices reales de imágenes
        val imageWrappers = oldItem.content.mapIndexedNotNull { index, content ->
            if (content is QuestionContent.Image) ContentWrapper(index, content) else null
        }

        if (isEditingMode) {
            val realIndex = imageWrappers[contadorContenido].originalIndex
            originalContent[realIndex] = newContent
        } else {
            originalContent.add(newContent)
        }

        mutableRefList[contadorPregunta] = oldItem.copy(content = originalContent.toList())

        resetEditingMode()
        resetContentLists()
        updateQuestions()
    }

    private fun getActualUri() = actualUri

    fun setActualUri(uri: Uri) {
        actualUri = uri
    }

    fun deleteImage(position: Int) {
        deleteFilteredContent(position, QuestionContent.Image::class.java)

        // Refrescar UI
        resetContentLists()
        updateQuestions()
    }

    fun deleteText(position: Int) {
        deleteFilteredContent(position, QuestionContent.Text::class.java)

        // Refrescar UI
        resetContentLists()
        updateQuestions()
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