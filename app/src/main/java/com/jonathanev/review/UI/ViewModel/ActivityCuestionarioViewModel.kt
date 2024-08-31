package com.jonathanev.review.UI.ViewModel

import android.app.Application
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.GuiasVerificacionModel
import com.jonathanev.review.Domain.setClickRegresarModicandoUseCase
import com.jonathanev.review.Domain.setClickSiguienteModificandoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivityCuestionarioViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val setClickRegresarModicandoUseCase: setClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: setClickSiguienteModificandoUseCase,
    application: Application
) : ViewModel() {
    var guias = MutableLiveData<List<GuiaModel>>()
    var colorAnterior = MutableLiveData<Int>()
    var saveClicked = MutableLiveData<Boolean>().apply { value = false }
    var rollClicked = MutableLiveData<Boolean>().apply { value = false }

    // Click events
    private val _buttonClickEventBack = MutableLiveData<Boolean>()
    val lvClickImgvPrevious: LiveData<Boolean> get() = _buttonClickEventBack
    private val _buttonClickEventNext = MutableLiveData<Boolean>()
    val lvClickImgvNext: LiveData<Boolean> get() = _buttonClickEventNext

    // Data Store
    private val dataStore = DataStoreManager.getInstance(application)
    val contImagenes = dataStore.getCountImage().asLiveData()

    fun getAllUpdatedGuides(file: File) {
        guias.postValue(guiaRepository.getGuias(file))
    }

    fun clickedSave() {
        saveClicked.postValue(!saveClicked.value!!)
    }

    fun clickedRoll() {
        rollClicked.postValue(!rollClicked.value!!)
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
    fun onClickImgvPrevious() {
        _buttonClickEventBack.value = true
    }

    fun onClickImgvNext() {
        _buttonClickEventNext.value = true
    }

    fun onClickRegresar(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ): GuiasVerificacionModel {
        return setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            isEtPregunta
        )
    }

    fun onClickSiguiente(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ): GuiasVerificacionModel {
        return setClickSiguienteModicandoUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            isEtPregunta
        )
    }
}