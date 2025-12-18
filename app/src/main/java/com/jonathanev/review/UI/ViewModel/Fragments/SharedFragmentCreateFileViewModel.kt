package com.jonathanev.review.UI.ViewModel.Fragments

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Core.Constants.VERSION1
import com.jonathanev.review.DI.IoDispatcher
import com.jonathanev.review.DI.MainDispatcher
import com.jonathanev.review.Data.Model.ContentWrapper
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.prueba.AnswerState
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.Model.prueba.UIStopEvent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Domain.GetAttributesGuideUseCase
import com.jonathanev.review.Domain.GetContentItemsUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.GetTextWithoutLabelsUseCase
import com.jonathanev.review.Domain.GetVersionUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import com.jonathanev.review.Domain.SetContentUseCase
import com.jonathanev.review.Domain.SetCrearXmlUseCase
import com.jonathanev.review.Domain.SetDecodePathImageUseCase
import com.jonathanev.review.Domain.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.inject.Inject

@HiltViewModel
class SharedFragmentCreateFileViewModel @Inject constructor(
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setContentUseCase: SetContentUseCase,
    private val setCrearXmlUseCase: SetCrearXmlUseCase,
    private val setDecodePathImageUseCase: SetDecodePathImageUseCase,
    private val getContentItemsUseCase: GetContentItemsUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getAttributesGuideUseCase: GetAttributesGuideUseCase,
    private val getVersionUseCase: GetVersionUseCase,
    private val fileRepository: FileRepository,
    private val filePathsProvider: FilePathsProvider,
    private val dataStore: DataStoreManager,
    private val guiaProvider: GuiaProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val getTextWithoutLabelsUseCase: GetTextWithoutLabelsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EstadoUI())
    val uiState = _uiState.asStateFlow()

    private val _UIStopEvent = MutableSharedFlow<UIStopEvent>()
    val uiStopEvent = _UIStopEvent.asSharedFlow()

    private var _preguntas = mutableListOf<QuestionItem>()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas = mutableListOf<QuestionItem>()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var _saveImages = MutableLiveData<List<QuestionContent.Image>>()
    val saveImages: LiveData<List<QuestionContent.Image>> get() = _saveImages

    private val _fileName = MutableLiveData<String>()
    val fileName: LiveData<String> get() = _fileName

    private var contadorContenido: Int = -1
    private lateinit var actualUri: Uri
    private var _typeContent: MutableLiveData<TypeContent> = MutableLiveData(TypeContent.QUESTION)
    val typeContent: LiveData<TypeContent> get() = _typeContent

    private var isEditingMode: Boolean = false

    private fun subtractCount() {
        _contadorPregunta--
    }

    private fun plusCount() {
        _contadorPregunta++
    }

    private fun swapTypeContent() {
        _typeContent.value =
            if (typeContent.value == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION
    }

    private fun setTypeContentWithQuestion() {
        _typeContent.value = TypeContent.QUESTION
    }

    private fun showContents() {
        val contentList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas
        val noContent = contentList.lastIndex

        if (contadorPregunta <= noContent) {
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
        val noContent = mutablePivRefList.lastIndex
        val newContent = QuestionContent.Text(textWithLabels, listSpans)

        // Si la lista está vacía y no estamos editando, creamos un nuevo item con el contenido.
        //if (mutablePivRefList.isEmpty()) {
        if (noContent < contadorPregunta) {
            mutablePivRefList.add(QuestionItem(content = listOf(newContent)))
            resetEditingMode()
            resetContentLists()
            showContents()
            return
        }

        setContentUseCase.invoke(
            newContent,
            mutablePivRefList,
            contadorPregunta,
            contadorContenido,
            isEditingMode,
            QuestionContent.Text::class.java
        )

        resetEditingMode()
        resetContentLists()
        showContents()
    }

    fun addImageContent() {
        val mutableRefList =
            if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas

        val actualUri = getActualUri().toString()
        //val encoded = setCifrarRutaImagenUseCase(actualUri, 26 - 3)
        val newContent = QuestionContent.Image(actualUri, "")
        val noContent = mutableRefList.lastIndex

        // Si la lista está vacía y no estamos editando -> crear nuevo item
        //if (mutableRefList.isEmpty()) {
        if (noContent < contadorPregunta) {
            mutableRefList.add(QuestionItem(content = listOf(newContent)))
            resetEditingMode()
            resetContentLists()
            showContents()
            return
        }

        setContentUseCase.invoke(
            newContent,
            mutableRefList,
            contadorPregunta,
            contadorContenido,
            isEditingMode,
            QuestionContent.Image::class.java
        )

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
        if (uiState.value.textList.isEmpty()) {
            viewModelScope.launch {
                _UIStopEvent.emit(UIStopEvent.ShowMessage("Debes tener al menos un texto"))
            }

            return
        }

        swapTypeContent()
        resetContentLists()
        showContents()
    }

    fun previousQuestion() {
        if (contadorPregunta == 0) {
            viewModelScope.launch {
                _UIStopEvent.emit(UIStopEvent.NotQuestionBefore("Ya no hay preguntas anteriores"))
            }

            return
        }

        // Revisa que haya un texto actualmente en pregunta/respuesta
        if (uiState.value.textList.isEmpty()) {
            viewModelScope.launch {
                _UIStopEvent.emit(UIStopEvent.ShowMessage("Debes tener al menos un texto"))
            }

            return
        }

        // Revisa que haya un texto en la misma posición para pregunta/respuesta
        val mutablePivRefList =
            if (typeContent.value == TypeContent.QUESTION) respuestas else preguntas
        val noContent = mutablePivRefList.lastIndex

        if (noContent < contadorPregunta) {
            emitShowMessage()
            return
        }

        val noTexts =
            mutablePivRefList[contadorPregunta].content
                .filterIsInstance<QuestionContent.Text>()
                .count()

        if (noTexts == 0) {
            emitShowMessage()
            return
        }

        setTypeContentWithQuestion()
        subtractCount()
        resetContentLists()
        showContents()
    }

    fun nextQuestion() {
        // Revisa que haya un texto actualmente en pregunta/respuesta
        if (uiState.value.textList.isEmpty()) {
            viewModelScope.launch {
                _UIStopEvent.emit(UIStopEvent.ShowMessage("Debes tener al menos un texto"))
            }

            return
        }

        // Revisa que haya un texto en la misma posición para pregunta/respuesta
        val mutablePivRefList =
            if (typeContent.value == TypeContent.QUESTION) respuestas else preguntas
        val noContent = mutablePivRefList.lastIndex

        if (noContent < contadorPregunta) {
            emitShowMessage()
            return
        }

        val noTexts =
            mutablePivRefList[contadorPregunta].content
                .filterIsInstance<QuestionContent.Text>()
                .count()

        if (noTexts == 0) {
            emitShowMessage()
            return
        }

        setTypeContentWithQuestion()
        plusCount()
        resetContentLists()
        showContents()
    }

    private fun emitShowMessage() {
        viewModelScope.launch {
            _UIStopEvent.emit(UIStopEvent.ShowMessage("Debes tener al menos un texto en pregunta/respuesta"))
        }
    }

    fun getCurrentPath() = fileRepository.getCurrentPath()

    fun getObtenerDatosXML(positionContent: Int) {
        setContadorPregunta(positionContent)

        if (respuestas.isEmpty()) {
            //Revisar como se obtienen los datos aqui, porque no se visualiza la imagen
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas = datos.mapNotNull { (it.answer as? AnswerState.Filled )?.item }.toMutableList()

            showContents()
        }
    }

    private fun setContadorPregunta(positionContent: Int) {
        _contadorPregunta = positionContent
    }

    private fun validationsSave(){
        // Revisa que haya un texto en la misma posición para pregunta/respuesta
        val questionExist =
            preguntas.getOrNull(0)
                ?.content
                ?.filterIsInstance<QuestionContent.Text>()
        val answerExist =
            respuestas.getOrNull(0)
                ?.content
                ?.filterIsInstance<QuestionContent.Text>()

        if (questionExist == null || answerExist == null) {
            viewModelScope.launch {
                _UIStopEvent.emit(UIStopEvent.ShowMessage("Debes tener como minimo una pregunta y respuesta"))
            }

            return
        }

        val qNoTexts =
            preguntas.getOrNull(contadorPregunta)
                ?.content
                ?.filterIsInstance<QuestionContent.Text>()
                ?.count()
        val rNoTexts =
            respuestas.getOrNull(contadorPregunta)
                ?.content
                ?.filterIsInstance<QuestionContent.Text>()
                ?.count()

        if (qNoTexts == null || rNoTexts == null) {
            viewModelScope.launch {
                _UIStopEvent.emit(UIStopEvent.ShowMessage("Asegurate de completar una pregunta y una respuesta"))
            }

            return
        }

        if (qNoTexts == 0 || rNoTexts == 0) {
            emitShowMessage()

            return
        }
    }

    fun saveOldGuide() {
        validationsSave()

        val currentPath = File(getCurrentPath())
        val imagesPath = currentPath.toString().replace(".xml", "")

        val images = File(imagesPath.replace("guias", "imagenes"))
        if (images.exists()) {
            images.deleteRecursively()
        }

        images.mkdirs()

        viewModelScope.launch {
            val version = getVersionUseCase.invoke(currentPath)
            setDecodePathImageUseCase.invoke(_preguntas, _respuestas)

            // Si existe el archivo
            if (version == VERSION1){
                val listImages = (preguntas + respuestas)
                    .flatMap { it.content }
                    .filterIsInstance<QuestionContent.Image>()

                listImages.forEach { image ->
                    val source = File(image.uri)
                    if (source.exists()){
                        val noImage = image.uri.substringAfterLast("/")
                        val newPathFile = filePathsProvider.buildImage(images, noImage)

                        Files.copy(
                            Paths.get(image.uri),
                            Paths.get(newPathFile.path),
                            StandardCopyOption.REPLACE_EXISTING
                        )

                        if (newPathFile.exists()){
                            File(image.uri).delete()
                        }
                    } else {
                        guiaProvider.saveImagesInDevice(listOf(image), images)
                    }
                }
            }

            val response = getAttributesGuideUseCase.invoke(currentPath)

            val isSuccess = setCrearXmlUseCase.invoke(
                response.nameGuide,
                response.description,
                currentPath,
                preguntas,
                respuestas
            )

            Log.i("Guardado", isSuccess.toString())
        }
    }

    fun saveNewGuide(nameGuide: String, description: String) {
        validationsSave()

        val currentPath = filePathsProvider.buildFile(File(getCurrentPath()), nameGuide)
        val imagesPath = filePathsProvider.buildFolder(File(getCurrentPath()), nameGuide).path

        val images = File(imagesPath.replace("guias", "imagenes"))
        if (images.exists()) {
            images.deleteRecursively()
        }

        images.mkdirs()

        viewModelScope.launch {
            setDecodePathImageUseCase.invoke(_preguntas, _respuestas)

            val listImages = (preguntas + respuestas)
                .flatMap { it.content }
                .filterIsInstance<QuestionContent.Image>()

            guiaProvider.saveImagesInDevice(listImages, images)

            val isSuccess = setCrearXmlUseCase.invoke(
                nameGuide,
                description,
                currentPath,
                preguntas,
                respuestas
            )

            Log.i("Guardado", isSuccess.toString())
        }
    }
}