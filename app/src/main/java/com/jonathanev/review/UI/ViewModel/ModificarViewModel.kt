package com.jonathanev.review.UI.ViewModel

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.DI.IoDispatcher
import com.jonathanev.review.DI.MainDispatcher
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetClickEliminarUseCase
import com.jonathanev.review.Domain.SetClickRegresarModicandoUseCase
import com.jonathanev.review.Domain.SetClickSaveUseCase
import com.jonathanev.review.Domain.SetClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.SetPintarLetraUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import com.jonathanev.review.Domain.SetRollClickedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.annotation.VisibleForTesting

@HiltViewModel
class ModificarViewModel @Inject constructor(
    //application: Application,
    private val setRollClickedUseCase: SetRollClickedUseCase,
    private val setClickRegresarModicandoUseCase: SetClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: SetClickSiguienteModificandoUseCase,
    private val setClickEliminarUseCase: SetClickEliminarUseCase,
    private val setClickSaveUseCase: SetClickSaveUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setPintarLetraUseCase: SetPintarLetraUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    private val getGuiaUseCase: GetGuiaUseCase,
    // Se pusieron aquí para facilitar las pruebas.
    // Solo para tests
    /*internal var dataStore: DataStoreManager? = null,
    internal var dispatcher: CoroutineDispatcher = Dispatchers.Main*/
    //private val dataStore: DataStoreManager = DataStoreManager.getInstance(application)
    private val dataStore: DataStoreManager,
    //private val dispatcher: CoroutineDispatcher = Dispatchers.Main
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

    /*private var preguntas: ArrayList<String> = ArrayList()
    private var respuestas: ArrayList<String> = ArrayList()
    private var contadorPregunta: Int = 0
    private var showMessageMoreQuestions: Boolean = true*/

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

    // para DataStore
    //private val dataStore = DataStoreManager.getInstance(application)
    //private val _contImagenes = dataStore.getCountImage().asLiveData()
    /*private val _contImagenes = dataStore.getCountImage().asLiveData()
    val contImagenes: LiveData<Int> get() = _contImagenes*/

    private val _contImagenes = MutableLiveData<Int>()
    val contImagenes: LiveData<Int> get() = _contImagenes

    // para GuiaModel
    private val _guiaModel = MutableLiveData<GuiaModel>()
    val guiaModel: LiveData<GuiaModel> get() = _guiaModel

    // Solo para tests
    //internal var dispatcher: CoroutineDispatcher = Dispatchers.Main

    /*val contImagenes = dataStore.getCountImage().asLiveData()
    val guiaModel = MutableLiveData<GuiaModel>()*/

    private val _textoImagenCorrutina = MutableLiveData<String>()
    val textoImagenCorrutina: LiveData<String> get() = _textoImagenCorrutina

    // var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    // var rollClicked = MutableLiveData<Boolean>().apply { value = false }

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

    fun getObtenerDatosXML(nombreArchivo: String, ruta: String): ValidacionesGuiaModel {
        //if (respuestas.isEmpty()) {
            _preguntas.clear()
            _respuestas.clear()

            val datos = getObtenerDatosXMLUseCase(nombreArchivo, ruta)
            datos.forEach { preguntaRespuesta ->
                _preguntas.add(preguntaRespuesta.pregunta)
                _respuestas.add(preguntaRespuesta.respuesta)
            }
        //}

        // Pinta la primer pregunta después de recuperar los datos
        val textoPregunta = setPintarTextosUseCase(isEtPregunta = true,
            preguntas = _preguntas,
            respuestas = _respuestas,
            contadorPregunta = contadorPregunta,
            ruta = ruta)
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
        val responseRegresarUseCase = setClickRegresarModicandoUseCase(
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
            setRollClickedUseCase(_preguntas, _respuestas, contadorPregunta, editable, isEtPregunta, ruta)
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
}