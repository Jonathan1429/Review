package com.jonathanev.review.UI.ViewModel

import android.app.Application
import android.content.Context
import android.text.Editable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
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
import com.jonathanev.review.Domain.setClickRegresarModicandoUseCase
import com.jonathanev.review.Domain.setClickSaveUseCase
import com.jonathanev.review.Domain.setClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.setCopyImagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivityCuestionarioViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val setClickRegresarModicandoUseCase: setClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: setClickSiguienteModificandoUseCase,
    private val setClickSaveUseCase: setClickSaveUseCase,
    private val setCopyImagesUseCase: setCopyImagesUseCase,
    application: Application
) : ViewModel() {
    var guias = MutableLiveData<List<GuiaModel>>()
    var colorAnterior = MutableLiveData<Int>()
    // var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    var rollClicked = MutableLiveData<Boolean>().apply { value = false }

    // Click events
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnSave = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnSave: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnSave
    /*private val _uiStateBtnSave = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnSave: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnSave*/

    // Data Store
    private val dataStore = DataStoreManager.getInstance(application)
    val contImagenes = dataStore.getCountImage().asLiveData()

    fun procesoActualizacion(){
        getAllUpdatedGuides(file)
        copyImages()
    }

    fun getAllUpdatedGuides(file: File) {
        guias.postValue(guiaRepository.getGuias(file))
    }

    fun copyImages(){
        setCopyImagesUseCase()
    }


    fun clickedRoll() {
        rollClicked.postValue(true)
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

    suspend fun setIncrementCounter() {
        dataStore.setIncrementCounter()
    }

    suspend fun resetCounter() {
        dataStore.resetCounter()
    }

    // Click events
    fun onClickImgvPrevious(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
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

        _uiStateBtnBack.value = responseRegresarUseCase
    }

    fun onClickImgvNext(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
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

        _uiStateBtnNext.value = responseSiguienteUseCase
    }

    fun onClickImgvSave(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
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
}