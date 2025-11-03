package com.jonathanev.review.UI.ViewModel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.GetClickRegresarUseCase
import com.jonathanev.review.Domain.GetClickSiguienteUseCase
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityRepasarGuiaViewModel @Inject constructor(
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    private val getGuiaUseCase: GetGuiaUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getClickRegresarUseCase: GetClickRegresarUseCase,
    private val getClickSiguienteUseCase: GetClickSiguienteUseCase,
    private val fileRepositoryImpl: FileRepositoryImpl,
    private val guiaProvider: GuiaProvider
) : ViewModel() {
    private var _preguntas: ArrayList<String> = ArrayList()
    val preguntas: ArrayList<String> get() = _preguntas

    private var _respuestas: ArrayList<String> = ArrayList()
    val respuestas: ArrayList<String> get() = _respuestas

    private var _contadorPregunta: Int = 0
    val contadorPregunta: Int get() = _contadorPregunta

    private var _guiaModel = MutableLiveData<GuiaModel>()
    val guiaModel: MutableLiveData<GuiaModel> get() = _guiaModel

    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnRoll = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnRoll: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnRoll

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

    fun getGuia(ruta: String) {
        _guiaModel.postValue(getGuiaUseCase(ruta))
    }

    fun getObtenerDatosXML(): ValidacionesGuiaModel {
        if (respuestas.isEmpty()) {
            _preguntas.clear()
            _respuestas.clear()

            val datos = getObtenerDatosXMLUseCase(getCurrentPath())
            datos.forEach { preguntaRespuesta ->
                _preguntas.add(preguntaRespuesta.pregunta)
                _respuestas.add(preguntaRespuesta.respuesta)
            }
        }

        val textoPregunta =
            setPintarTextosUseCase(
                isEtPregunta = true,
                preguntas = preguntas,
                respuestas = respuestas,
                contadorPregunta = contadorPregunta,
                ruta = getCurrentPath()
            )

        val responseValGuiaModel: ValidacionesGuiaModel =
            textoPregunta.copy(
                estadoUI = textoPregunta.estadoUI.copy(isThereMoreAsks = true)
            )

        return responseValGuiaModel
    }

    fun onClickRoll(isEtPregunta: Boolean, ruta: String) {
        val texto = setPintarTextosUseCase(
            isEtPregunta,
            preguntas,
            respuestas,
            contadorPregunta,
            ruta
        )

        _uiStateBtnRoll.value = texto
    }

    fun getReinicioGuia(isEtPregunta: Boolean, ruta: String): ValidacionesGuiaModel {
        val texto = setPintarTextosUseCase(
            isEtPregunta,
            preguntas,
            respuestas,
            contadorPregunta,
            ruta
        )

        return texto
    }

    fun onClickNext(ruta:String) {
        val responseSiguiente = getClickSiguienteUseCase(contadorPregunta, preguntas, respuestas, ruta)

        if (responseSiguiente.estadoUI.isUpdatedAskAns) {
            _contadorPregunta++
        }

        _uiStateBtnNext.value = responseSiguiente
    }

    fun onClickBefore(ruta: String) {
        val responseRegresar = getClickRegresarUseCase(contadorPregunta, preguntas, respuestas, ruta)

        if (responseRegresar.estadoUI.isUpdatedAskAns) {
            _contadorPregunta--
        }

        _uiStateBtnBack.value = responseRegresar
    }

    fun onResetContadorPreg(){
        _contadorPregunta = 0
    }

    fun getCurrentPath() = fileRepositoryImpl.getCurrentPath()

    /*@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun setContadorPreguntaTest(value: Int) {
        _contadorPregunta = value
    }*/

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun setRespuestas(value: ArrayList<String>) {
        _respuestas = value
    }

    fun prueba(): List<GuiaModel> {
        return guiaProvider.guias
    }
}