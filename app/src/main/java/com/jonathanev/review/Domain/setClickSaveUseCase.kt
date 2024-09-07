package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class setClickSaveUseCase @Inject constructor(
    private val setSpanPalabraUseCase: setSpanPalabraUseCase,
    private val setCrearXmlUseCasel: setCrearXmlUseCasel
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        nombreArchivo: String,
        isEtPregunta: Boolean
    ): ValidacionesGuiaModel {
        val posPregFin = preguntas.size - 1
        val posRespFin = respuestas.size - 1
        val responseSpanPalabra = setSpanPalabraUseCase(editable)

        return when {
            responseSpanPalabra.editable.isEmpty() || isEtPregunta && posPregFin != posRespFin -> {
                if (isEtPregunta && posPregFin > -1) {
                    // Se tiene que guardar la guia y crear el archivo
                    setCrearXmlUseCasel(nombreArchivo, preguntas, respuestas)
                } else {
                    ValidacionesGuiaModel(
                        message = "Asegurate de llenar pregunta y respuesta",
                        estadoUI = EstadoUI(isUpdatedAskAns = false)
                    )
                }
            }

            else -> {
                // Label pregunta
                if (isEtPregunta) {
                    preguntas[contadorPregunta] = responseSpanPalabra.editable.toString()
                } else {
                    if (contadorPregunta > posRespFin) {
                        respuestas.add(contadorPregunta, responseSpanPalabra.editable.toString())
                    } else {
                        respuestas[contadorPregunta] = responseSpanPalabra.editable.toString()
                    }
                }

                // Se tiene que guardar la guia y crear el archivo
                setCrearXmlUseCasel(nombreArchivo, preguntas, respuestas)
            }
        }
    }
}