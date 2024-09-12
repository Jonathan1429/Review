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
import com.jonathanev.review.Domain.setClickRegresarModicandoUseCase
import com.jonathanev.review.Domain.setClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.setRollClickedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModificarViewModel @Inject constructor(
    application: Application,
    private val setRollClickedUseCase: setRollClickedUseCase,
    private val setClickRegresarModicandoUseCase: setClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: setClickSiguienteModificandoUseCase,
    private val getObtenerDatosXMLUseCase: getObtenerDatosXMLUseCase,
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

    private val _uiShowDates = MutableLiveData<Boolean>()
    val uiShowDates: LiveData<Boolean> get() = _uiShowDates

    private val dataStore = DataStoreManager.getInstance(application)
    val contImagenes = dataStore.getCountImage().asLiveData()
    val guiaModel = MutableLiveData<GuiaModel>()
    var colorAnterior = MutableLiveData<Int>()
    var saveClicked = MutableLiveData<Boolean>().apply { value = false }

    // var rollClicked = MutableLiveData<Boolean>().apply { value = false }

    // Data Store
    fun getCountImage() {
        viewModelScope.launch {
            dataStore.getCountImage()
        }
    }

    fun llamaCorruIncremento() {
        viewModelScope.launch {
            setIncrementCounter()
        }
    }

    suspend fun setIncrementCounter() {
        dataStore.setIncrementCounter()
    }

    suspend fun resetCounter() {
        dataStore.resetCounter()
    }

    fun getGuia(ruta: String) {
        guiaModel.postValue(getGuiaUseCase(ruta))
    }

    fun clickedSave() {
        saveClicked.postValue(!saveClicked.value!!)
    }

    fun getObtenerDatosXML(nombreArchivo: String, ruta: String) {
        val datos = getObtenerDatosXMLUseCase(nombreArchivo, ruta)
        datos.forEach { preguntaRespuesta ->
            preguntas.add(preguntaRespuesta.pregunta)
            respuestas.add(preguntaRespuesta.respuesta)
        }

        if (preguntas.isNotEmpty()){
            _uiShowDates.value = true
        }
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
}