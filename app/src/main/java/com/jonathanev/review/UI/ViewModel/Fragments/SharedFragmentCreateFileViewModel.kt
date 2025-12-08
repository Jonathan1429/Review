package com.jonathanev.review.UI.ViewModel.Fragments

import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.InternalRules
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Domain.GetQuestionContentsUseCase
import com.jonathanev.review.Domain.GetTextWithoutLabelsUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val getQuestionContentsUseCase: GetQuestionContentsUseCase,
    private val getTextWithoutLabelsUseCase: GetTextWithoutLabelsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EstadoUI())
    val uiState = _uiState.asStateFlow()

    private var _preguntas = mutableListOf<QuestionItem>()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas = mutableListOf<QuestionItem>()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var contadorContenido: Int = -1

    // Dentro de FragCreateFolderViewModel, FragMainActivity, etc.
    private val _typeContent = MutableStateFlow(TypeContent.QUESTION)

    // Exponemos el flujo observable (la Vista lo lee)
    val typeContent: StateFlow<TypeContent> = _typeContent.asStateFlow()

    private var isEditingMode: Boolean = false

    private fun getCount() = contadorPregunta

    fun setColocarEtiquetas(text: String, listSpans: List<ColorRange>): String {
        return setColocarEtiquetasUseCase.invoke(text, listSpans)
    }

    fun setEditingMode(value: Boolean, position: Int) {
        isEditingMode = value
        contadorContenido = position
    }

    fun addTextContent(textWithLabels: String, listSpans: List<ColorRange>) {
        val mutableRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas
        val newContent = QuestionContent.Text(textWithLabels, listSpans)

        if (isEditingMode) {
            // Crear una NUEVA lista reemplazando el elemento
            val newContentList = mutableRefList[contadorPregunta].content.toMutableList().apply {
                this[contadorContenido] = newContent
            }.toList() // vuelve a convertir a List inmutable (opcional)

            // Crear un nuevo QuestionItem con esa lista
            val updatedItem = mutableRefList[contadorPregunta].copy(content = newContentList)

            // Reemplazar el item en la lista principal
            mutableRefList[contadorPregunta] = updatedItem
        } else if (mutableRefList.isNotEmpty()) {
            val oldItem = mutableRefList[contadorPregunta]

            // Crear copia mutable del contenido
            val newContentList = oldItem.content.toMutableList().apply {
                add(newContent)
            }.toList()

            // Crear nuevo item
            val updatedItem = oldItem.copy(content = newContentList)

            // Reemplazar en la lista principal
            mutableRefList[contadorPregunta] = updatedItem
        } else {
            val newItem = QuestionItem(content = listOf(newContent))
            mutableRefList.add(newItem)
        }

        isEditingMode = false
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
}