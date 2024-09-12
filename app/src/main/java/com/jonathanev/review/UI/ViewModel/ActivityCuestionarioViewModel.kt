package com.jonathanev.review.UI.ViewModel

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Core.Constants.file
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.setCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.setClickEliminarUseCase
import com.jonathanev.review.Domain.setClickRegresarModicandoUseCase
import com.jonathanev.review.Domain.setClickSaveUseCase
import com.jonathanev.review.Domain.setClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.setCopyImagesUseCase
import com.jonathanev.review.Domain.setPintarLetraUseCase
import com.jonathanev.review.Domain.setRollClickedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivityCuestionarioViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val setClickRegresarModicandoUseCase: setClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: setClickSiguienteModificandoUseCase,
    private val setRollClickedUseCase: setRollClickedUseCase,
    private val setClickSaveUseCase: setClickSaveUseCase,
    private val setClickEliminarUseCase: setClickEliminarUseCase,
    private val setCopyImagesUseCase: setCopyImagesUseCase,
    private val setCifrarRutaImagenUseCase: setCifrarRutaImagenUseCase,
    private val setPintarLetraUseCase: setPintarLetraUseCase,
    application: Application
) : ViewModel() {
    private var preguntas: ArrayList<String> = ArrayList()
    private var respuestas: ArrayList<String> = ArrayList()
    private var contadorPregunta: Int = 0

    var guias = MutableLiveData<List<GuiaModel>>()
    // var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    // var rollClicked = MutableLiveData<Boolean>().apply { value = false }

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
    private val dataStore = DataStoreManager.getInstance(application)
    val contImagenes = dataStore.getCountImage().asLiveData()

    fun procesoActualizacion() {
        getAllUpdatedGuides(file)
        copyImages()
    }

    private fun getAllUpdatedGuides(file: File) {
        guias.postValue(guiaRepository.getGuias(file))
    }

    private fun copyImages() {
        setCopyImagesUseCase()
    }


    fun clickedRoll(
        editable: Editable,
        isEtPregunta: Boolean
    ) {
        val responseRollClickedUseCase =
            setRollClickedUseCase(preguntas, respuestas, contadorPregunta, editable, isEtPregunta)
        _uiStateBtnRoll.value = responseRollClickedUseCase
    }

    // Data Store
    fun getCountImage() {
        dataStore.getCountImage()
    }

    fun llamaCorruIncremento() {
        viewModelScope.launch {
            setIncrementCounter()
        }
    }

    private suspend fun setIncrementCounter() {
        dataStore.setIncrementCounter()
    }

    suspend fun resetCounter() {
        dataStore.resetCounter()
    }

    fun getUrlImagenCifrada(urlImagen: String, noCifrado: Int):String {
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

    fun onClickImgvSave(
        editable: Editable,
        nombreArchivo: String,
        isEtPregunta: Boolean
    ) {
        val setClickSaveUseCase = setClickSaveUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            nombreArchivo,
            isEtPregunta
        )

        _uiStateBtnSave.value = setClickSaveUseCase
    }

    fun onClickEliminar() {
        val responseRegresarUseCase =
            setClickEliminarUseCase(preguntas, respuestas, contadorPregunta)

        if (contadorPregunta > 0) {
            contadorPregunta--
        }

        _uiStateBtnEliminar.value = responseRegresarUseCase
    }
}