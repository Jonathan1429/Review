package com.jonathanev.review.UI.ViewModel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.DI.IoDispatcher
import com.jonathanev.review.DI.MainDispatcher
import com.jonathanev.review.data.Model.DataStoreManager
import com.jonathanev.review.data.Model.EstadoUI
import com.jonathanev.review.data.Model.GuideModel
import com.jonathanev.review.data.Model.InternalRules
import com.jonathanev.review.data.Model.MessageActions
import com.jonathanev.review.presentation.state.AnswerState
import com.jonathanev.review.presentation.model.ColorRange
import com.jonathanev.review.presentation.model.QAUiItem
import com.jonathanev.review.presentation.model.QuestionContent
import com.jonathanev.review.presentation.model.QuestionItem
import com.jonathanev.review.Domain.model.TypeContent
import com.jonathanev.review.data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentInPivUseCase
import com.jonathanev.review.Domain.DeleteCurrentQuestionUseCase
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import com.jonathanev.review.Domain.SetCreatePivImage
import com.jonathanev.review.UI.Utils.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivityModificarViewModel @Inject constructor(
    //application: Application,
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setCreatePivImage: SetCreatePivImage,
    private val deleteCurrentQuestionUseCase: DeleteCurrentQuestionUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getGuiaUseCase: GetGuiaUseCase,
    private val deleteContentInPivUseCase: DeleteContentInPivUseCase,
    private val dataStore: DataStoreManager,
    private val fileRepositoryImpl: FileRepositoryImpl,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {
    private var _preguntas = mutableListOf<QuestionItem>()
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private var _respuestas = mutableListOf<QuestionItem>()
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    // Dentro de FragCreateFolderViewModel, FragMainActivity, etc.
    private val _typeContent = MutableStateFlow(TypeContent.QUESTION)
    // Exponemos el flujo observable (la Vista lo lee)
    val typeContent: StateFlow<TypeContent> = _typeContent.asStateFlow()

    private var _showMessageMoreQuestions = true
    val showMessageMoreQuestions: Boolean get() = _showMessageMoreQuestions

    private val _navigateToNext = MutableSharedFlow<QAUiItem>()
    val navigateToNext = _navigateToNext.asSharedFlow()

    /*private val _uiEvent = MutableLiveData<UiEvent>()
    val uiEvent get() = _uiEvent*/

    //private var _currentPath = MutableStateFlow(getCurrentPathUseCase())
    // El StateFlow del VM ahora es una simple copia del Flow del Repositorio.
    // Usamos 'StateFlow' del repositorio para el estado de la UI del VM.
    val currentPath: StateFlow<String> = fileRepositoryImpl.currentPathFlow
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed asegura que solo se recolecte cuando la UI esté activa.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = fileRepositoryImpl.getCurrentPath() // Valor inicial de la ruta
        )

    private val _uiState = MutableStateFlow(EstadoUI())
    val uiState = _uiState.asStateFlow()

    // Click events
    /*private val _uiStateBtnRoll = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnRoll: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnRoll
    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnEliminar = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnEliminar: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnEliminar
    private val _uiStateBtnSave = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnSave: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnSave*/

    private val _contImagenes = MutableLiveData<Int>()
    val contImagenes: LiveData<Int> get() = _contImagenes

    // para GuiaModel
    private val _guideModel = MutableLiveData<GuideModel>()
    val guideModel: LiveData<GuideModel> get() = _guideModel
    private val _textoImagenCorrutina = MutableLiveData<String>()
    val textoImagenCorrutina: LiveData<String> get() = _textoImagenCorrutina

    private var currentContent: QuestionContent = QuestionContent.None
    private var previousContent: QuestionContent = QuestionContent.None

    fun setContent(newContent: QuestionContent) {
        previousContent = currentContent
        currentContent = newContent
    }

    fun shouldWarnImageReplace(): Boolean {
        return previousContent is QuestionContent.Image && currentContent is QuestionContent.Image
    }

    //fun getCurrentContent(): QuestionContent = currentContent

    // Data Store
    fun getCountImage() {
        viewModelScope.launch {
            dataStore.getCountImage().collect { count ->
                _contImagenes.value = count
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun setContadorPreguntaTest(value: Int) {
        _contadorPregunta = value
    }

    fun llamaCorruIncremento(cifrado: String) {
        //viewModelScope.launch(Dispatchers.Main) {
        viewModelScope.launch(mainDispatcher) {
            setIncrementCounter()

            //_textoImagenCorrutina.value = cifrado
            _textoImagenCorrutina.postValue(cifrado)
        }
    }

    private suspend fun setIncrementCounter() {
        withContext(ioDispatcher) {
            dataStore.setIncrementCounter()
        }
    }

    suspend fun resetCounter() {
        dataStore.resetCounter()
    }

    fun setPlusCountQuestion() {
        _contadorPregunta++
    }

    fun setMinusCountQuestion() {
        _contadorPregunta--
    }

    fun setCrearXML() {
        //val isSuccess = setCrearXmlUseCase.invoke(getCurrentPath(), preguntas, respuestas)

        if (true) {
            val qaItem = QAUiItem(
                preguntas = preguntas.map { it.toUi() },
                respuestas = respuestas.map { it.toUi() }
            )

            viewModelScope.launch { _navigateToNext.emit(qaItem) }
        }
    }

    fun getGuia() {
        _guideModel.postValue(getGuiaUseCase(ruta = fileRepositoryImpl.getCurrentPath()))
    }

    fun getObtenerDatosXML() {
        if (respuestas.isEmpty()) {
            val datos = getObtenerDatosXMLUseCase.invoke(ruta = getCurrentPath())

            _preguntas = datos.map { it.question }.toMutableList()
            _respuestas = datos.mapNotNull { (it.answer as? AnswerState.Filled )?.item }.toMutableList()
        }

        cargarPregunta(typeContent.value)
    }

    fun onClickRoll(text: String): MessageActions {
        if (text.isEmpty()) {
            return MessageActions.FieldEmpty
        }

        return MessageActions.Continue
    }

    fun onClickBefore(text: String): MessageActions {
        val contador = contadorPregunta - 1

        if (contador < 0) {
            return MessageActions.WithoutQuestionsBefore
        }

        if (text.isEmpty()) {
            return MessageActions.FieldEmpty
        }

        return MessageActions.Continue
    }

    fun onClickNext(text: String): MessageActions {
        val posPregFin = preguntas.size - 1
        val contador = contadorPregunta + 1

        if (text.isEmpty()) {
            return MessageActions.FieldEmpty
        }

        // Don't show alert
        if (!showMessageMoreQuestions) {
            return MessageActions.Continue
        }

        if (contador > posPregFin) {
            MessageActions.AddMoreQuestions
        }

        return MessageActions.Continue
    }

    fun onClickEliminar(): MessageActions {
        if (contadorPregunta == 0) {
            _uiState.value = EstadoUI(
                internalRules = InternalRules(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true
                ),
            )
            return MessageActions.WithoutQuestionsBefore
        }

        return MessageActions.Continue
    }

    /*fun onClickImgvSave(text: String): MessageActions {
        val posRespFin = respuestas.size - 1

        if (text.isEmpty() && (contadorPregunta <= posRespFin || _typeContent == TypeContent.QUESTION)) {
            return MessageActions.FieldEmpty
        }

        return MessageActions.Continue
    }*/

    fun deleteCurrentQuestion() {
        deleteCurrentQuestionUseCase.invoke(_preguntas, _respuestas, contadorPregunta)
    }

    fun updateQuestion(textWithLabels: String, listSpans: List<ColorRange>) {
        // La lista no crea una copia sino que apunta al mismo punto de referencia.
        // Esto hace que cuando se modifique algo afecta _preguntas o _respuestas
        val mutableRefList = if (typeContent.value == TypeContent.QUESTION) _preguntas else _respuestas
        val posTotales = mutableRefList.lastIndex
        setContent(QuestionContent.Text(textWithLabels, listSpans))
        val currentItem = QuestionItem(mutableListOf(currentContent))

        if (contadorPregunta <= posTotales) {
            mutableRefList[contadorPregunta] = currentItem
        } else {
            mutableRefList.add(contadorPregunta, currentItem)
        }
    }

    fun setColocarEtiquetas(text: String, listSpans: List<ColorRange>): String {
        return setColocarEtiquetasUseCase.invoke(text, listSpans)
    }

    fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()

    fun swapTypeContent() {
        _typeContent.value =
            if (typeContent.value == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION
    }

    fun setTypeContent(value: TypeContent) {
        _typeContent.value = value
    }

    /*fun getTypeContent(): TypeContent {
        return _typeContent
    }*/

    fun cargarPregunta(typeContent: TypeContent, shouldFlip: Boolean = false) {
        /*val contentList = getContentItemsUseCase.invoke(
            if (typeContent == TypeContent.QUESTION) preguntas else respuestas,
            contadorPregunta
        )

        contentList.forEach { item ->
            when (val result = setPintarTextosUseCase(item, getCurrentPath())) {

                is QuestionContent.Image -> {
                    _uiState.value = EstadoUI(
                        shouldFlip = shouldFlip,
                        internalRules = InternalRules(isShowCancelar = true),
                        content = result
                    )
                }

                is QuestionContent.Text -> {
                    _uiState.value = EstadoUI(
                        shouldFlip = shouldFlip,
                        internalRules = InternalRules(
                            isShowQuitColor = true,
                            isShowSelColor = true
                        ),
                        content = result
                    )
                }

                QuestionContent.None -> _uiState.value = EstadoUI()
            }
        }*/
    }

    fun deleteContentInPiv() {
        val fileSelected = getCurrentPath().substringAfterLast("/")
        deleteContentInPivUseCase.invoke(fileSelected)
    }

    fun getUrlImagenCifrada(urlImagen: String, noCifrado: Int): String {
        return setCifrarRutaImagenUseCase.invoke(urlImagen, noCifrado)
    }

    fun setCreatePivImage(originPath: File, copiedPath: File, noImage: String) {
        setCreatePivImage.invoke(originPath, copiedPath, noImage)
    }

    fun toggleShowMessageMoreQuestions() {
        _showMessageMoreQuestions = !_showMessageMoreQuestions
    }

    /*fun setPintarLetra(texto: Editable, cursorPosition: Int, colorActual: Int) {
        setPintarLetraUseCase.invoke(texto, cursorPosition, colorActual)
    }*/

    /*fun getGuia(ruta: String) {
        _guiaModel.postValue(getGuiaUseCase(ruta))
    }

    fun getObtenerDatosXML(): ValidacionesGuiaModel {
        //if (respuestas.isEmpty()) {
        _preguntas.clear()
        _respuestas.clear()

        val datos = getObtenerDatosXMLUseCase(getCurrentPath())
        datos.forEach { preguntaRespuesta ->
            _preguntas.add(preguntaRespuesta.pregunta)
            _respuestas.add(preguntaRespuesta.respuesta)
        }
        //}

        // Pinta la primer pregunta después de recuperar los datos
        val textoPregunta = setPintarTextosUseCase(
            isEtPregunta = true,
            question = _preguntas,
            answer = _respuestas,
            contadorPregunta = contadorPregunta,
            ruta = getCurrentPath()
        )
        val responseValGuiaModel: ValidacionesGuiaModel =
            textoPregunta.copy(
                estadoUI = textoPregunta.estadoUI.copy(isThereMoreAsks = true)
            )

        return responseValGuiaModel
    }

    fun getUrlImagenCifrada(urlImagen: String, noCifrado: Int): String {
        return setCifrarRutaImagenUseCase(urlImagen, noCifrado)
    }

    fun setPintarLetra(texto: Editable, cursorPosition: Int, colorActual: Int) {
        setPintarLetraUseCase(texto, cursorPosition, colorActual)
    }

    // Click events
    fun onClickImgvPrevious(
        editable: Editable,
        isEtPregunta: Boolean,
        ruta: String
    ) {
        val responseRegresarUseCase = setClickRegresarModificandoUseCase(
            _preguntas,
            _respuestas,
            contadorPregunta,
            editable,
            isEtPregunta,
            ruta
        )

        if (responseRegresarUseCase.estadoUI.isUpdatedAskAns) {
            _contadorPregunta--
        }

        _uiStateBtnBack.value = responseRegresarUseCase
    }

    fun onClickImgvNext(
        editable: Editable,
        isEtPregunta: Boolean,
        ruta: String
    ) {
        val responseSiguienteUseCase = setClickSiguienteModicandoUseCase(
            _preguntas,
            _respuestas,
            contadorPregunta,
            editable,
            isEtPregunta,
            ruta
        )

        if (responseSiguienteUseCase.estadoUI.isUpdatedAskAns) {
            _contadorPregunta++
        }

        _uiStateBtnNext.value = responseSiguienteUseCase
    }

    fun clickedRoll(
        editable: Editable,
        isEtPregunta: Boolean,
        ruta: String
    ) {
        val responseRollClickedUseCase =
            setRollClickedUseCase(
                _preguntas,
                _respuestas,
                contadorPregunta,
                editable,
                isEtPregunta,
                ruta
            )
        _uiStateBtnRoll.value = responseRollClickedUseCase
    }

    fun onClickEliminar(ruta: String) {
        val responseRegresarUseCase =
            setClickEliminarUseCase(_preguntas, _respuestas, contadorPregunta, ruta)

        if (contadorPregunta > 0) {
            _contadorPregunta--
        }

        _uiStateBtnEliminar.value = responseRegresarUseCase
    }

    fun onClickImgvSave(
        editable: Editable,
        nombreArchivo: String,
        isEtPregunta: Boolean,
        didTheGuideAlreadyExist: Boolean,
        ruta: String
    ) {
        val setClickSaveUseCase = setClickSaveUseCase(
            _preguntas,
            _respuestas,
            contadorPregunta,
            editable,
            nombreArchivo,
            isEtPregunta,
            didTheGuideAlreadyExist,
            ruta
        )

        _uiStateBtnSave.value = setClickSaveUseCase
    }

    fun toggleShowMessageMoreQuestions() {
        _showMessageMoreQuestions = !_showMessageMoreQuestions
    }

    fun imagePathConverted(uiState: ValidacionesGuiaModel): String {
        val currentFolder = currentPath.value.substringAfterLast("/")
        val imagen = uiState.estadoImagen.textImgUnencrypted.substringAfterLast("/")
        val imagePath = filePathsProvider.buildFileFolder(filePathsProvider.fileImages,
            currentFolder, imagen)

        // Verificar si la imagen se encuentra en ruta Pivote o Imagenes
        val rutaImagenExistente = if (imagePath.exists()) {
            imagePath.toString()
        } else {
            filePathsProvider.buildFile(
                filePathsProvider.fileImagesPiv, imagen
            ).toString()
        }

        return rutaImagenExistente
    }

    fun toCopyImages(){
        setCopyImagesUseCase.invoke()
    }

    fun deleteContentInPiv(nombreArchivo: String) {
        deleteContentInPivUseCase.invoke(nombreArchivo)
    }

    fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()*/
}