package com.jonathanev.review.UI.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Domain.getGuiaUseCase
import com.jonathanev.review.Domain.getObtenerDatosXMLUseCase
import com.jonathanev.review.Domain.setPintarTextosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RepasarGuiaViewModel @Inject constructor(
    private val setPintarTextosUseCase: setPintarTextosUseCase,
    private val getGuiaUseCase: getGuiaUseCase,
    private val getObtenerDatosXMLUseCase: getObtenerDatosXMLUseCase
): ViewModel(){
    var preguntas: ArrayList<String> = ArrayList()
    private var respuestas: ArrayList<String> = ArrayList()
    var contadorPregunta: Int = 0

    val guiaModel = MutableLiveData<GuiaModel>()

    fun getGuia(ruta: String){
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

    fun getPintarTexto(isEtPregunta: Boolean): ValidacionesGuiaModel {
        val texto = setPintarTextosUseCase(isEtPregunta, preguntas, respuestas, contadorPregunta)
        return texto
    }


}