package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class setClickEliminarUseCase @Inject constructor(
    private val setPintarTextosUseCase: setPintarTextosUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int
    ): ValidacionesGuiaModel {
        val pospregfin = preguntas.size - 1
        val posRespFin = respuestas.size - 1

        if (contadorPregunta <= pospregfin) {
            preguntas.removeAt(contadorPregunta)
        }

        if (contadorPregunta <= posRespFin) {
            respuestas.removeAt(contadorPregunta)
        }

        if (contadorPregunta > 0) {
            val contador = contadorPregunta - 1
            val validacionesGuiaModel =
                setPintarTextosUseCase(true, preguntas, respuestas, contador)
            val response = validacionesGuiaModel.copy(
                estadoUI = validacionesGuiaModel.estadoUI.copy(
                    isThereMoreAsks = true
                )
            )

            return response
        } else {
            return ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true
                )
            )
        }
    }
}