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
import com.jonathanev.review.Data.Model.SpanPalabraModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.setClickRegresarModicandoUseCase
import com.jonathanev.review.Domain.setClickSiguienteModificandoUseCase
import com.jonathanev.review.Domain.setColocarEtiquetasUseCase
import com.jonathanev.review.Domain.setPintarTextosUseCase
import com.jonathanev.review.Domain.setSpanPalabraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ActivityCuestionarioViewModel @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val setClickRegresarModicandoUseCase: setClickRegresarModicandoUseCase,
    private val setClickSiguienteModicandoUseCase: setClickSiguienteModificandoUseCase,
    private val setSpanPalabraUseCase: setSpanPalabraUseCase,
    private val setPintarTextosUseCase: setPintarTextosUseCase,
    private val setColocarEtiquetas: setColocarEtiquetasUseCase,
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

    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack

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
    fun onClickImgvPrevious(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ) {
        // val responseSpanPalabra = setSpan(editable)
        // val responseEtiquetaEditable = setEtiquetas(responseSpanPalabra.editable)
        val setClickRegresarModicandoUseCase = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            isEtPregunta
        )

        // Se actualizan las validaciones de las guias.
        _uiStateBtnBack.value = ValidacionesGuiaModel(
            setClickRegresarModicandoUseCase.message,
            setClickRegresarModicandoUseCase.responseSpanPalabra,
            setClickRegresarModicandoUseCase.textImgEcrypted,
            setClickRegresarModicandoUseCase.textImgUnencrypted,
            setClickRegresarModicandoUseCase.contadorPregunta,
            setClickRegresarModicandoUseCase.isUpdatedAskAns,
            setClickRegresarModicandoUseCase.isClearText,
            setClickRegresarModicandoUseCase.isShowImage,
            setClickRegresarModicandoUseCase.isShowCancelar,
            setClickRegresarModicandoUseCase.isShowQuitColor,
            setClickRegresarModicandoUseCase.isShowSelColor,
            setClickRegresarModicandoUseCase.isThereMoreAsks,
            setClickRegresarModicandoUseCase.builder,
            setClickRegresarModicandoUseCase.preguntas,
            setClickRegresarModicandoUseCase.respuestas
        )
    }

    fun onClickImgvNext(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ) {
        // _buttonClickEventNext.value = true
        val setClickSiguienteModicandoUseCase = setClickSiguienteModicandoUseCase(
            preguntas,
            respuestas,
            contadorPregunta,
            editable,
            isEtPregunta
        )

        // Se actualizan las validaciones de las guias.
        _uiStateBtnNext.value = ValidacionesGuiaModel(
            setClickSiguienteModicandoUseCase.message,
            setClickSiguienteModicandoUseCase.responseSpanPalabra,
            setClickSiguienteModicandoUseCase.textImgEcrypted,
            setClickSiguienteModicandoUseCase.textImgUnencrypted,
            setClickSiguienteModicandoUseCase.contadorPregunta,
            setClickSiguienteModicandoUseCase.isUpdatedAskAns,
            setClickSiguienteModicandoUseCase.isClearText,
            setClickSiguienteModicandoUseCase.isShowImage,
            setClickSiguienteModicandoUseCase.isShowCancelar,
            setClickSiguienteModicandoUseCase.isShowQuitColor,
            setClickSiguienteModicandoUseCase.isShowSelColor,
            setClickSiguienteModicandoUseCase.isThereMoreAsks,
            setClickSiguienteModicandoUseCase.builder,
            setClickSiguienteModicandoUseCase.preguntas,
            setClickSiguienteModicandoUseCase.respuestas
        )
    }

    fun setSpan(editable: Editable): SpanPalabraModel {
        return setSpanPalabraUseCase(editable)
    }

    fun setEtiquetas(editable: Editable): Editable {
        return setColocarEtiquetas(editable)
    }
}