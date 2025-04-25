package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class SetClickSaveUseCase @Inject constructor(
    private val setSpanPalabraUseCase: SetSpanPalabraUseCase,
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setCrearXmlUseCase: SetCrearXmlUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        nombreArchivo: String,
        isEtPregunta: Boolean,
        didTheGuideAlreadyExist: Boolean,
        ruta: String
    ): ValidacionesGuiaModel {
        val posRespFin = respuestas.size - 1
        val responseSpanPalabra = setSpanPalabraUseCase(editable)
        val responseEtiquetaEditable = setColocarEtiquetasUseCase(responseSpanPalabra.editable)

        return when {
            responseSpanPalabra.editable.isEmpty()-> {
                if (contadorPregunta <= posRespFin || !isEtPregunta){
                    ValidacionesGuiaModel(
                        message = "Asegurate de llenar pregunta y respuesta",
                        estadoUI = EstadoUI(isUpdatedAskAns = false)
                    )
                } else {
                    setCrearXmlUseCase(nombreArchivo, preguntas, respuestas, didTheGuideAlreadyExist, ruta)
                }
            }
            else -> {
                if (contadorPregunta <= posRespFin){
                    if (isEtPregunta) {
                        preguntas[contadorPregunta] = responseEtiquetaEditable.toString()
                    } else {
                        respuestas[contadorPregunta] = responseEtiquetaEditable.toString()
                    }
                } else {
                    respuestas.add(contadorPregunta, responseEtiquetaEditable.toString())
                }

                setCrearXmlUseCase(nombreArchivo, preguntas, respuestas, didTheGuideAlreadyExist, ruta)
            }
        }
    }
}