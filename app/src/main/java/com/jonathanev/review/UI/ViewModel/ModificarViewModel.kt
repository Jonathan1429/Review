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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ModificarViewModel @Inject constructor(
    application: Application,
    private val setRollClickedUseCase: SetRollClickedUseCase,
    private val setClickRegresarModicandoUseCase: SetClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: SetClickSiguienteModificandoUseCase,
    private val setClickEliminarUseCase: SetClickEliminarUseCase,
    private val setClickSaveUseCase: SetClickSaveUseCase,
    private val setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
    private val setPintarLetraUseCase: SetPintarLetraUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    val getGuiaUseCase: GetGuiaUseCase
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
        if (respuestas.isEmpty()) {
            preguntas.clear()
            respuestas.clear()

            val datos = getObtenerDatosXMLUseCase(nombreArchivo, ruta)
            datos.forEach { preguntaRespuesta ->
                preguntas.add(preguntaRespuesta.pregunta)
                respuestas.add(preguntaRespuesta.respuesta)
            }
        }

        val textoPregunta = setPintarTextosUseCase(isEtPregunta = true,
            preguntas = preguntas,
            respuestas = respuestas,
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
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            isEtPregunta,
            ruta
        )

        if (responseRegresarUseCase.estadoUI.isUpdatedAskAns) {
            contadorPregunta--
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
            contadorPregunta++
        }

        _uiStateBtnNext.value = responseSiguienteUseCase
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

    fun onClickEliminar(ruta: String) {
        val responseRegresarUseCase =
            setClickEliminarUseCase(preguntas, respuestas, contadorPregunta, ruta)

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