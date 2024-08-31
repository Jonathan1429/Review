package com.jonathanev.review.Domain

import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Toast
import com.jonathanev.review.Data.Model.GuiasVerificacionModel
import javax.inject.Inject

class setClickSiguienteModificandoUseCase @Inject constructor() {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean
    ): GuiasVerificacionModel {
        // Validamos campos vacios en la pregunta o respuesta.
        // val longi: Int = respuestas.size - 1
        val posPregFin = preguntas.size - 1
        val posRespFin = respuestas.size - 1

        return when {
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
                    message = "Asegurate de llenar pregunta y respuesta",
                    contadorPregunta = contadorPregunta + 1,
                    shouldUpdateLabel = true,
                    preguntas = preguntas,
                    respuestas = respuestas
                )
            }
        }

        /*if (binding!!.etPregResp.text.toString().isEmpty() || (binding!!.etPregResp.text.toString().isNotEmpty() && contadorPregunta > posPregFin)){
            Toast.makeText(
                applicationContext,
                "Asegurate de llenar pregunta y respuesta",
                Toast.LENGTH_SHORT
            ).show()

            Log.i("Crear pregunta: ", "Asegurate de no dejar ningun campo vacio")
        } else {
            setSpanPalabra()

            var isEtPregunta = false
            if (binding!!.lblPregResp.text.toString() == "Pregunta") {
                isEtPregunta = true
            }

            if (isEtPregunta) {
                preguntas[contadorPregunta] = binding!!.etPregResp.text.toString()
            } else {
                if (contadorPregunta > posRespFin) {
                    respuestas.add(contadorPregunta, binding!!.etPregResp.text.toString())
                } else {
                    respuestas[contadorPregunta] = binding!!.etPregResp.text.toString()
                }
            }

            // Mientras el contadorPregunta sea menor escribiremos la siguiente pregunta
            // en los et.
            contadorPregunta++
            if (contadorPregunta <= posPregFin) {
                // Pintamos el texto en la pregunta actual
                binding!!.lblPregResp.text = "Pregunta"
                pintarTexto(contadorPregunta)
            } else {
                // binding!!.lblPregResp.text = "Pregunta"
                binding!!.tilContenidoPregResp.visibility = View.VISIBLE
                binding!!.ivImagen.visibility = View.GONE
                binding!!.etPregResp.text?.clear()
            }

            binding!!.imgvCancelar.visibility = View.GONE
            binding!!.imgvQuitColor.visibility = View.VISIBLE
            binding!!.imgvSelColor.visibility = View.VISIBLE

            binding!!.lblPregResp.text = "Pregunta"
        }*/
    }
}