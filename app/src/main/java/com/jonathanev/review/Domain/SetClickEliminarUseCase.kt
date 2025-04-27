package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import javax.inject.Inject

class SetClickEliminarUseCase @Inject constructor(
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        preguntas: ArrayList<String>,
        respuestas: ArrayList<String>,
        contadorPregunta: Int,
        ruta: String
    ): ValidacionesGuiaModel {
        preguntas.removeAtOrNull(contadorPregunta)
        respuestas.removeAtOrNull(contadorPregunta)

        return if (contadorPregunta > 0) {
            val validacionesGuiaModel =
                setPintarTextosUseCase(true, preguntas, respuestas, contadorPregunta - 1, ruta)

            validacionesGuiaModel.copy(
                estadoUI = validacionesGuiaModel.estadoUI.copy(
                    isThereMoreAsks = true
                )
            )
        } else {
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true
                )
            )
        }
    }

    private fun ArrayList<String>.removeAtOrNull(index: Int) {
        if (index in indices) removeAt(index)
    }
}