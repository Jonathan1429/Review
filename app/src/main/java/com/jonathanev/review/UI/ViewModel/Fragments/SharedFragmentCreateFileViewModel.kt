package com.jonathanev.review.UI.ViewModel.Fragments

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.ContentWrapper
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.InternalRules
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Domain.GetQuestionContentsUseCase
import com.jonathanev.review.Domain.GetTextWithoutLabelsUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
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
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
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

    private val uriImages: MutableList<Uri> = mutableListOf()

    private lateinit var actualUri: Uri

    // Dentro de FragCreateFolderViewModel, FragMainActivity, etc.
    private val _typeContent = MutableStateFlow(TypeContent.QUESTION)

    // Exponemos el flujo observable (la Vista lo lee)
    val typeContent: StateFlow<TypeContent> = _typeContent.asStateFlow()

    private var isEditingMode: Boolean = false

    fun getCount() = contadorPregunta

    fun setColocarEtiquetas(text: String, listSpans: List<ColorRange>): String {
        return setColocarEtiquetasUseCase.invoke(text, listSpans)
    }

    fun setEditingMode(value: Boolean, position: Int) {
        isEditingMode = value
        contadorContenido = position
    }

    fun addTextContent(textWithLabels: String, listSpans: List<ColorRange>) {
        val mutablePivRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        val newContent = QuestionContent.Text(textWithLabels, listSpans)

        // Lista original
        val originalItem = mutablePivRefList[contadorPregunta]
        val originalContent = originalItem.content.toMutableList()

        // Lista filtrada con índices reales
        val textWrappers = originalItem.content.mapIndexedNotNull { index, item ->
            if (item is QuestionContent.Text)
                ContentWrapper(index, item)
            else null
        }

        if (isEditingMode) {
            // wrapper seleccionado por posición en el recycler
            val selectedWrapper = textWrappers[contadorContenido]

            val originalIndex = selectedWrapper.originalIndex

            // reemplazar en lista original
            originalContent[originalIndex] = newContent

        } else {
            // agregar un texto nuevo
            originalContent.add(newContent)
        }

        // guardar cambios en QuestionItem
        mutablePivRefList[contadorPregunta] = originalItem.copy(
            content = originalContent.toList()
        )

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

    fun addUriImagesSelected(uri: Uri) {
        val mutableRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        processImage(mutableRefList, uri)
        resetContentLists()
        updateQuestions()
    }

    private fun processImage(mutableRefList: MutableList<QuestionItem>, uri: Uri) {

        val encoded = setCifrarRutaImagenUseCase(uri.toString(), 26 - 3)
        val newContent = QuestionContent.Image(uri.toString(), encoded)

        if (mutableRefList.isEmpty()) {

            // Caso inicial: solo agregas directamente
            val newItem = QuestionItem(content = listOf(newContent))
            mutableRefList.add(newItem)

        } else {

            val oldItem = mutableRefList[contadorPregunta]

            // 🔹 Mapeo a wrappers para NO perder los índices originales
            val wrappers = oldItem.content.mapIndexed { index, content ->
                ContentWrapper(index, content)
            }.toMutableList()

            // 🔹 Agregar el nuevo contenido SIN romper índices anteriores
            wrappers.add(
                ContentWrapper(
                    originalIndex = wrappers.size, // el índice real nuevo
                    content = newContent
                )
            )

            // 🔹 Volver a una lista normal de QuestionContent
            val newContentList = wrappers.map { it.content }

            // 🔹 Actualizar item
            val updatedItem = oldItem.copy(content = newContentList)
            mutableRefList[contadorPregunta] = updatedItem
        }
    }

    fun replaceUriImages(uri: Uri, posImage: Int) {
        val encoded = setCifrarRutaImagenUseCase(uri.toString(), 26 - 3)
        val newContent = QuestionContent.Image(uri.toString(), encoded)

        val mutableRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas
        // Crear una NUEVA lista reemplazando el elemento
        val newContentList = mutableRefList[contadorPregunta].content.toMutableList().apply {
            this[posImage] = newContent
        }.toList() // vuelve a convertir a List inmutable (opcional)

        // Crear un nuevo QuestionItem con esa lista
        val updatedItem = mutableRefList[contadorPregunta].copy(content = newContentList)

        // Reemplazar el item en la lista principal
        mutableRefList[contadorPregunta] = updatedItem

        resetContentLists()
        updateQuestions()
    }

    fun getActualUri() = actualUri

    fun setActualUri(uri: Uri) {
        actualUri = uri
    }

    fun getActualList(): MutableList<QuestionItem> {
        return if (typeContent.value == TypeContent.QUESTION) preguntas else respuestas
    }
}