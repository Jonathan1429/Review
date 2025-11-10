package com.jonathanev.review.UI.ViewModel

import android.text.Editable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.DI.IoDispatcher
import com.jonathanev.review.DI.MainDispatcher
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentInPivUseCase
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetClickEliminarUseCase
import com.jonathanev.review.Domain.SetClickRegresarModificandoUseCase
import com.jonathanev.review.Domain.SetClickSaveUseCase
import com.jonathanev.review.Domain.SetClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.SetCopyImagesUseCase
import com.jonathanev.review.Domain.SetPintarLetraUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import com.jonathanev.review.Domain.SetRollClickedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ActivityModificarViewModel @Inject constructor(
    //application: Application,
    private val setRollClickedUseCase: SetRollClickedUseCase,
    private val setClickRegresarModificandoUseCase: SetClickRegresarModificandoUseCase,
    private val setClickSiguienteModicandoUseCase: SetClickSiguienteModificandoUseCase,
    private val setClickEliminarUseCase: SetClickEliminarUseCase,
    private val setClickSaveUseCase: SetClickSaveUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setPintarLetraUseCase: SetPintarLetraUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    private val setCopyImagesUseCase: SetCopyImagesUseCase,
    private val getGuiaUseCase: GetGuiaUseCase,
    private val deleteContentInPivUseCase: DeleteContentInPivUseCase,
    private val dataStore: DataStoreManager,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val filePathsProvider: FilePathsProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {
    private var _preguntas: ArrayList<String> = ArrayList()
    val preguntas: List<String> get() = _preguntas

    private var _respuestas: ArrayList<String> = ArrayList()
    val respuestas: List<String> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var _showMessageMoreQuestions = true
    val showMessageMoreQuestions: Boolean get() = _showMessageMoreQuestions

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

    // Click events
    private val _uiStateBtnRoll = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnRoll: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnRoll
    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnEliminar = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnEliminar: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnEliminar
    private val _uiStateBtnSave = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnSave: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnSave

    private val _contImagenes = MutableLiveData<Int>()
    val contImagenes: LiveData<Int> get() = _contImagenes

    // para GuiaModel
    private val _guiaModel = MutableLiveData<GuiaModel>()
    val guiaModel: LiveData<GuiaModel> get() = _guiaModel
    private val _textoImagenCorrutina = MutableLiveData<String>()
    val textoImagenCorrutina: LiveData<String> get() = _textoImagenCorrutina

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

    fun getGuia(ruta: String) {
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

    fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()
}