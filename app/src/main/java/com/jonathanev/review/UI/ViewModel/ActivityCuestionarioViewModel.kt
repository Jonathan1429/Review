package com.jonathanev.review.UI.ViewModel

import android.text.Editable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.DI.IoDispatcher
import com.jonathanev.review.DI.MainDispatcher
import com.jonathanev.review.Data.GuiaRepositoryImpl
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.GuideModel
import com.jonathanev.review.Data.Model.InternalRules
import com.jonathanev.review.Data.Model.MessageActions
import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QAUiItem
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.TypeContent
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentInPivUseCase
import com.jonathanev.review.Domain.DeleteCurrentQuestionUseCase
import com.jonathanev.review.Domain.GetContentItemsUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetColocarEtiquetasUseCase
import com.jonathanev.review.Domain.SetCopyImagesUseCase
import com.jonathanev.review.Domain.SetCrearXmlUseCase
import com.jonathanev.review.Domain.SetPintarLetraUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
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
import javax.inject.Inject

@HiltViewModel
class ActivityCuestionarioViewModel @Inject constructor(
    private val guiaRepositoryImpl: GuiaRepositoryImpl,
    private val getContentItemsUseCase: GetContentItemsUseCase,
    private val setCrearXmlUseCase: SetCrearXmlUseCase,
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val deleteCurrentQuestionUseCase: DeleteCurrentQuestionUseCase,
    private val setCopyImagesUseCase: SetCopyImagesUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    private val setPintarLetraUseCase: SetPintarLetraUseCase,
    private val deleteContentInPivUseCase: DeleteContentInPivUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val dataStore: DataStoreManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
    //application: Application
) : ViewModel() {
    private lateinit var _preguntas: MutableList<QuestionItem>
    val preguntas: MutableList<QuestionItem> get() = _preguntas

    private lateinit var _respuestas: MutableList<QuestionItem>
    val respuestas: MutableList<QuestionItem> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var _showMessageMoreQuestions = true
    val showMessageMoreQuestions: Boolean get() = _showMessageMoreQuestions

    private val _navigateToNext = MutableSharedFlow<QAUiItem>()
    val navigateToNext = _navigateToNext.asSharedFlow()

    //var guias = MutableLiveData<List<GuiaModel>>()
    private val _guias = MutableLiveData<List<GuideModel>>()
    val guias: LiveData<List<GuideModel>> get() = _guias

    /*// Click events
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnSave = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnSave: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnSave
    private val _uiStateBtnRoll = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnRoll: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnRoll
    private val _uiStateBtnEliminar = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnEliminar: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnEliminar*/

    // Data Store
    //private val dataStore = DataStoreManager.getInstance(application)
    private val _contImagenes = MutableLiveData<Int>()
    val contImagenes: LiveData<Int> get() = _contImagenes

    // Dentro de FragCreateFolderViewModel, FragMainActivity, etc.
    private val _typeContent = MutableStateFlow(TypeContent.QUESTION)
    // Exponemos el flujo observable (la Vista lo lee)
    val typeContent: StateFlow<TypeContent> = _typeContent.asStateFlow()

    private val _textoImagenCorrutina = MutableLiveData<String>()
    val textoImagenCorrutina: LiveData<String> get() = _textoImagenCorrutina

    private var currentContent: QuestionContent = QuestionContent.None
    private var previousContent: QuestionContent = QuestionContent.None

    private val _uiState = MutableStateFlow(EstadoUI())
    val uiState = _uiState.asStateFlow()

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

    //val contImagenes = dataStore.getCountImage().asLiveData()

    /*fun procesoActualizacion() {
        getAllUpdatedGuides(filePathsProvider.fileGuides)
        copyImages()
    }*/

    /*private fun getAllUpdatedGuides(file: File) {
        _guias.postValue(guiaRepository.getGuias(file))
    }*/

    private fun copyImages() {
        setCopyImagesUseCase()
    }

    // Data Store
    fun getCountImage() {
        viewModelScope.launch {
            dataStore.getCountImage().collect { count ->
                _contImagenes.value = count
            }
        }
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

    fun getUrlImagenCifrada(urlImagen: String, noCifrado: Int): String {
        return setCifrarRutaImagenUseCase(urlImagen, noCifrado)
    }

    fun setPintarLetra(texto: Editable, cursorPosition: Int, colorActual: Int) {
        setPintarLetraUseCase(texto, cursorPosition, colorActual)
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

    fun deleteContentInPiv(nombreArchivo: String) {
        deleteContentInPivUseCase.invoke(nombreArchivo)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun setContadorPreguntaTest(value: Int) {
        _contadorPregunta = value
    }

    fun onClickRoll(text: String): Any {
        if (text.isEmpty()) {
            return MessageActions.FieldEmpty
        }

        return MessageActions.Continue
    }

    fun swapTypeContent() {
        _typeContent.value =
            if (typeContent.value == TypeContent.QUESTION) TypeContent.ANSWER else TypeContent.QUESTION
    }

    fun setColocarEtiquetas(text: String, listSpans: List<ColorRange>): String {
        return setColocarEtiquetasUseCase.invoke(text, listSpans)
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

    fun setContent(newContent: QuestionContent) {
        previousContent = currentContent
        currentContent = newContent
    }

    /*fun getTypeContent(): TypeContent {
        return _typeContent
    }*/

    fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()

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

    fun setMinusCountQuestion() {
        _contadorPregunta--
    }

    fun setPlusCountQuestion() {
        _contadorPregunta++
    }

    fun setTypeContent(value: TypeContent) {
        _typeContent.value = value
    }

    fun toggleShowMessageMoreQuestions() {
        _showMessageMoreQuestions = !_showMessageMoreQuestions
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

    fun deleteCurrentQuestion() {
        deleteCurrentQuestionUseCase.invoke(_preguntas, _respuestas, contadorPregunta)
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
}