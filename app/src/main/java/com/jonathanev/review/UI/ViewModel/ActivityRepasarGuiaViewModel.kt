package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.GetClickRegresarUseCase
import com.jonathanev.review.Domain.GetClickSiguienteUseCase
import com.jonathanev.review.Domain.GetGuiaUseCase
import com.jonathanev.review.Domain.GetObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.SetPintarTextosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityRepasarGuiaViewModel @Inject constructor(
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    private val getGuiaUseCase: GetGuiaUseCase,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val getClickRegresarUseCase: GetClickRegresarUseCase,
    private val getClickSiguienteUseCase: GetClickSiguienteUseCase
) : ViewModel() {
    var preguntas: ArrayList<String> = ArrayList()
    private var respuestas: ArrayList<String> = ArrayList()
    var contadorPregunta: Int = 0

    val guiaModel = MutableLiveData<GuiaModel>()

    private val _uiStateBtnBack = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnBack: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnBack
    private val _uiStateBtnNext = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnNext: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnNext
    private val _uiStateBtnRoll = MutableLiveData<ValidacionesGuiaModel>()
    val uiStateBtnRoll: LiveData<ValidacionesGuiaModel> get() = _uiStateBtnRoll

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

        val textoPregunta =
            setPintarTextosUseCase(true, preguntas, respuestas, contadorPregunta, ruta)
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
            contadorPregunta++
        }

        _uiStateBtnNext.value = responseSiguiente
    }

    fun onClickBefore(ruta: String) {
        val responseRegresar = getClickRegresarUseCase(contadorPregunta, preguntas, respuestas, ruta)

        if (responseRegresar.estadoUI.isUpdatedAskAns) {
            contadorPregunta--
        }

        _uiStateBtnBack.value = responseRegresar
    }
}