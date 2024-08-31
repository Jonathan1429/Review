package com.jonathanev.review.Domain

import android.text.Editable
import javax.inject.Inject
import com.jonathanev.review.Data.Model.GuiasVerificacionModel

class setClickRegresarModicandoUseCase @Inject constructor() {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ): GuiasVerificacionModel{
        val posPregFin = preguntas.size - 1
        val posRespFin = respuestas.size - 1

        return when {
            contadorPregunta == 0 -> {
                GuiasVerificacionModel(
                    message = "Ya no tienes preguntas anteriores",
                    contadorPregunta = contadorPregunta,
                    shouldUpdateLabel = false,
                    preguntas = preguntas,
                    respuestas = respuestas
                )
            }
            contadorPregunta > posPregFin && contadorPregunta > posRespFin && editable.toString().isEmpty() -> {
                GuiasVerificacionModel(
                    message = null,  // No se necesita mensaje de toast
                    contadorPregunta = contadorPregunta - 1,
                    shouldUpdateLabel = true,
                    preguntas = preguntas,
                    respuestas = respuestas
                )
            }
            editable.toString().isEmpty() || (editable.toString().isNotEmpty() && contadorPregunta > posPregFin) -> {
                GuiasVerificacionModel(
                    message = "Asegurate de llenar pregunta y respuesta",
                    contadorPregunta = contadorPregunta,
                    shouldUpdateLabel = false,
                    preguntas = preguntas,
                    respuestas = respuestas
                )
            }
            else -> {
                if (isEtPregunta) {
                    preguntas[contadorPregunta] = editable.toString()
                } else {
                    if (contadorPregunta > posRespFin) {
                        respuestas.add(contadorPregunta, editable.toString())
                    } else {
                        respuestas[contadorPregunta] = editable.toString()
                    }
                }

                GuiasVerificacionModel(
                    message = null,
                    contadorPregunta = contadorPregunta - 1,
                    shouldUpdateLabel = true,
                    preguntas = preguntas,
                    respuestas = respuestas
                )
            }
        }
    }
}
