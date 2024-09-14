package com.jonathanev.review.UI.ViewModel

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.getGuiaUseCase
import com.jonathanev.review.Domain.getObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.setCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.setClickEliminarUseCase
import com.jonathanev.review.Domain.setClickRegresarModicandoUseCase
import com.jonathanev.review.Domain.setClickSaveUseCase
import com.jonathanev.review.Domain.setClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.setPintarLetraUseCase
import com.jonathanev.review.Domain.setPintarTextosUseCase
import com.jonathanev.review.Domain.setRollClickedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ModificarViewModel @Inject constructor(
    application: Application,
    private val setRollClickedUseCase: setRollClickedUseCase,
    private val setClickRegresarModicandoUseCase: setClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: setClickSiguienteModificandoUseCase,
    private val setClickEliminarUseCase: setClickEliminarUseCase,
    private val setClickSaveUseCase: setClickSaveUseCase,
    private val setCifrarRutaImagenUseCase: setCifrarRutaImagenUseCase,
    private val setPintarLetraUseCase: setPintarLetraUseCase,
    private val getObtenerDatosXMLUseCase: getObtenerDatosXMLUseCase,
    private val setPintarTextosUseCase: setPintarTextosUseCase,
    val getGuiaUseCase: getGuiaUseCase
) : ViewModel() {
    var preguntas: ArrayList<String> = ArrayList()
    private var respuestas: ArrayList<String> = ArrayList()
    var contadorPregunta: Int = 0
    var showMessageMoreQuestions: Boolean = true

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

    private val dataStore = DataStoreManager.getInstance(application)
    val contImagenes = dataStore.getCountImage().asLiveData()
    val guiaModel = MutableLiveData<GuiaModel>()

    private val _textoImagenCorrutina = MutableLiveData<String>()
    val textoImagenCorrutina: LiveData<String> get() = _textoImagenCorrutina

    // var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    // var rollClicked = MutableLiveData<Boolean>().apply { value = false }

    // Data Store
    fun getCountImage() {
        viewModelScope.launch {
            dataStore.getCountImage()
        }
    }

    fun llamaCorruIncremento(cifrado: String) {
        viewModelScope.launch(Dispatchers.Main) {
            setIncrementCounter()

            _textoImagenCorrutina.value = cifrado
        }
    }

    private suspend fun setIncrementCounter() {
        withContext(Dispatchers.IO) {
            dataStore.setIncrementCounter()
        }
    }

    suspend fun resetCounter() {
        dataStore.resetCounter()
    }

    fun getGuia(ruta: String) {
        guiaModel.postValue(getGuiaUseCase(ruta))
    }

    fun getObtenerDatosXML(nombreArchivo: String, ruta: String): ValidacionesGuiaModel {
        val datos = getObtenerDatosXMLUseCase(nombreArchivo, ruta)
        datos.forEach { preguntaRespuesta ->
            preguntas.add(preguntaRespuesta.pregunta)
            respuestas.add(preguntaRespuesta.respuesta)
        }

        val textoPregunta = setPintarTextosUseCase(true, preguntas, respuestas, contadorPregunta)
        val responseValGuiaModel: ValidacionesGuiaModel =
            textoPregunta.copy(
                estadoUI = textoPregunta.estadoUI.copy(isThereMoreAsks = true)
            )

        return responseValGuiaModel
    }

    fun getUrlImagenCifrada(urlImagen: String, noCifrado: Int): String {
        return setCifrarRutaImagenUseCase(urlImagen, noCifrado)
    }

    fun setPintarLetra(texto: Editable?, cursorPosition: Int, colorActual: Int) {
        setPintarLetraUseCase(texto, cursorPosition, colorActual)
    }

    // Click events
    fun onClickImgvPrevious(
        editable: Editable,
        isEtPregunta: Boolean
    ) {
        val responseRegresarUseCase = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            isEtPregunta
        )

        if (responseRegresarUseCase.estadoUI.isUpdatedAskAns) {
            contadorPregunta--
        }

        _uiStateBtnBack.value = responseRegresarUseCase
    }

    fun onClickImgvNext(
        editable: Editable,
        isEtPregunta: Boolean
    ) {
        val responseSiguienteUseCase = setClickSiguienteModicandoUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            isEtPregunta
        )

        if (responseSiguienteUseCase.estadoUI.isUpdatedAskAns) {
            contadorPregunta++
        }

        _uiStateBtnNext.value = responseSiguienteUseCase
    }

    fun clickedRoll(
        editable: Editable,
        isEtPregunta: Boolean
    ) {
        val responseRollClickedUseCase =
            setRollClickedUseCase(preguntas, respuestas, contadorPregunta, editable, isEtPregunta)
        _uiStateBtnRoll.value = responseRollClickedUseCase
    }

    fun onClickEliminar() {
        val responseRegresarUseCase =
            setClickEliminarUseCase(preguntas, respuestas, contadorPregunta)

        if (contadorPregunta > 0) {
            contadorPregunta--
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
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            nombreArchivo,
            isEtPregunta,
            didTheGuideAlreadyExist,
            ruta
        )

        _uiStateBtnSave.value = setClickSaveUseCase
    }
}