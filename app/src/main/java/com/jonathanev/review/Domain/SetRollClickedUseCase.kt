package com.jonathanev.review.Domain

import android.text.Editable
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class SetRollClickedUseCase @Inject constructor(
    private val setSpanPalabraUseCase: SetSpanPalabraUseCase,
    private val setColocarEtiquetasUseCase: SetColocarEtiquetasUseCase,
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        editable: Editable,
        isEtPregunta: Boolean,
        ruta: String
    ): ValidacionesGuiaModel {
        /*if (editable.isEmpty()) {
            return ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta"
            )
        }*/

        /*val responseSpanPalabra = setSpanPalabraUseCase.invoke(editable)
        val responseEtiquetaEditable = setColocarEtiquetasUseCase.invoke(responseSpanPalabra.editable)*/

        val listaDestino = if (isEtPregunta) preguntas else respuestas
        val pintarLista = if (!isEtPregunta) preguntas else respuestas
        val posTotales = listaDestino.lastIndex

        if (contadorPregunta <= posTotales) {
            listaDestino[contadorPregunta] = responseEtiquetaEditable.toString()
        } else {
            listaDestino.add(contadorPregunta, responseEtiquetaEditable.toString())
        }

        // Pintamos la pregunta/respuesta si existe
        if (contadorPregunta <= pintarLista.lastIndex) {
            val responsePintarTextos = setPintarTextosUseCase(
                isEtPregunta = !isEtPregunta, // Pintas lo opuesto
                question = preguntas,
                answer = respuestas,
                contadorPregunta = contadorPregunta,
                ruta
            )

            return responsePintarTextos.copy(
                responseSpanPalabra = responseSpanPalabra,
                estadoUI = responsePintarTextos.estadoUI.copy(
                    isEtPregunta = !isEtPregunta
                )
            )
        }

        // Cuando se regresa la respuesta vacia se activan estas banderas
        return ValidacionesGuiaModel(
            estadoUI = EstadoUI(
                isUpdatedAskAns = true,
                isClearText = true,
                isShowQuitColor = true,
                isShowSelColor = true,
                isEtPregunta = false
            ),
            responseSpanPalabra = responseSpanPalabra
        )
    }
}