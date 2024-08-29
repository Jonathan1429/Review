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
                // Aquí deberías manejar lo de setSpanPalabra() y colocarEtiquetas() en funciones separadas
                // Ejemplo:
                // setSpanPalabra()
                // colocarEtiquetas(colorSpans, editable)

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

        /*val posPregFin = preguntas.size - 1
        val posRespFin = respuestas.size - 1
        if (contadorPregunta == 0) {
            // Mostrar mensaje de no hay preguntas anteriores
            Toast.makeText(
                applicationContext,
                "Ya no tienes preguntas anteriores",
                Toast.LENGTH_LONG
            ).show()

            Log.i("Crear pregunta: ", "Ya no tienes preguntas anteriores")
        } else if (contadorPregunta > posPregFin && contadorPregunta > posRespFin && binding!!.etPregResp.text.toString()
                .isEmpty()
        ) {
            // Si ambos campos están vacíos
            binding!!.lblPregResp.text = "Pregunta"
            contadorPregunta--
            // pintarTexto(contadorPregunta)
        } else if (binding!!.etPregResp.text.toString()
                .isEmpty() || (binding!!.etPregResp.text.toString()
                .isNotEmpty() && contadorPregunta > posPregFin)
        ) {
            // Mostrar mensaje de llenar pregunta y respuesta
            Toast.makeText(
                applicationContext,
                "Asegurate de llenar pregunta y respuesta",
                Toast.LENGTH_SHORT
            ).show()

            Log.i("Crear pregunta: ", "Asegurate de llenar pregunta y respuesta")
        } else {
            setSpanPalabra()
            val editable: Editable =
                Editable.Factory.getInstance().newEditable(binding!!.etPregResp.text)
            val colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
                0,
                editable.length,
                ForegroundColorSpan::class.java
            )
            colocarEtiquetas(colorSpans, editable)

            if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                preguntas[contadorPregunta] = editable.toString()
            } else {
                if (contadorPregunta > posRespFin) {
                    respuestas.add(contadorPregunta, editable.toString())
                } else {
                    respuestas[contadorPregunta] = editable.toString()
                }
            }

            binding!!.lblPregResp.text = "Pregunta"
            contadorPregunta--
            pintarTexto(contadorPregunta)
        }*/
    }
}
