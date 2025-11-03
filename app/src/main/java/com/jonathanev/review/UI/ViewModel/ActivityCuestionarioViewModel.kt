package com.jonathanev.review.UI.ViewModel

import android.text.Editable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.DI.IoDispatcher
import com.jonathanev.review.DI.MainDispatcher
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.DeleteContentInPivUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetClickEliminarUseCase
import com.jonathanev.review.Domain.SetClickRegresarModificandoUseCase
import com.jonathanev.review.Domain.SetClickSaveUseCase
import com.jonathanev.review.Domain.SetClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.SetCopyImagesUseCase
import com.jonathanev.review.Domain.SetPintarLetraUseCase
import com.jonathanev.review.Domain.SetRollClickedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivityCuestionarioViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val setClickRegresarModificandoUseCase: SetClickRegresarModificandoUseCase,
    private val setClickSiguienteModicandoUseCase: SetClickSiguienteModificandoUseCase,
    private val setRollClickedUseCase: SetRollClickedUseCase,
    private val setClickSaveUseCase: SetClickSaveUseCase,
    private val setClickEliminarUseCase: SetClickEliminarUseCase,
    private val setCopyImagesUseCase: SetCopyImagesUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setPintarLetraUseCase: SetPintarLetraUseCase,
    private val deleteContentInPivUseCase: DeleteContentInPivUseCase,
    private val filePathsProvider: FilePathsProvider,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val dataStore: DataStoreManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
    //application: Application
) : ViewModel() {
    private var _preguntas: ArrayList<String> = ArrayList()
    val preguntas: ArrayList<String> get() = _preguntas

    private var _respuestas: ArrayList<String> = ArrayList()
    val respuestas: ArrayList<String> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    //var guias = MutableLiveData<List<GuiaModel>>()
    private val _guias = MutableLiveData<List<GuiaModel>>()
    val guias: LiveData<List<GuiaModel>> get() = _guias

    // Click events
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnSave = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnSave: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnSave
    private val _uiStateBtnRoll = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnRoll: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnRoll
    private val _uiStateBtnEliminar = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnEliminar: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnEliminar

    // Data Store
    //private val dataStore = DataStoreManager.getInstance(application)
    private val _contImagenes = MutableLiveData<Int>()
    val contImagenes: LiveData<Int> get() = _contImagenes

    private val _textoImagenCorrutina = MutableLiveData<String>()
    val textoImagenCorrutina: LiveData<String> get() = _textoImagenCorrutina

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

    fun procesoActualizacion() {
        getAllUpdatedGuides(filePathsProvider.fileGuides)
        copyImages()
    }

    private fun getAllUpdatedGuides(file: File) {
        _guias.postValue(guiaRepository.getGuias(file))
    }

    private fun copyImages() {
        setCopyImagesUseCase()
    }


    fun clickedRoll(
        editable: Editable,
        isEtPregunta: Boolean,
        ruta: String
    ) {
        val responseRollClickedUseCase =
            setRollClickedUseCase(preguntas, respuestas, contadorPregunta, editable, isEtPregunta, ruta)
        _uiStateBtnRoll.value = responseRollClickedUseCase
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

    // Click events
    fun onClickImgvPrevious(
        editable: Editable,
        isEtPregunta: Boolean,
        ruta: String
    ) {
        val responseRegresarUseCase = setClickRegresarModificandoUseCase(
            preguntas,
            respuestas,
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
            preguntas,
            respuestas,
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

    fun onClickImgvSave(
        editable: Editable,
        nombreArchivo: String,
        isEtPregunta: Boolean,
        ruta: String
    ) {
        val setClickSaveUseCase = setClickSaveUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            nombreArchivo,
            isEtPregunta,
            false,
            ruta
        )

        _uiStateBtnSave.value = setClickSaveUseCase
    }

    fun onClickEliminar(ruta: String) {
        val responseRegresarUseCase =
            setClickEliminarUseCase(preguntas, respuestas, contadorPregunta, ruta)

        if (contadorPregunta > 0) {
            _contadorPregunta--
        }

        _uiStateBtnEliminar.value = responseRegresarUseCase
    }

    fun deleteContentInPiv(nombreArchivo: String) {
        deleteContentInPivUseCase.invoke(nombreArchivo)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun setContadorPreguntaTest(value: Int) {
        _contadorPregunta = value
    }
}