package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.prueba.QuestionItem
import javax.inject.Inject

class SetClickEliminarUseCase @Inject constructor(
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>,
        contadorPregunta: Int,
        //ruta: String
    ): Pair<MutableList<QuestionItem>, MutableList<QuestionItem>> {
        if (contadorPregunta in preguntas.indices) preguntas.removeAt(contadorPregunta)
        if (contadorPregunta in respuestas.indices) respuestas.removeAt(contadorPregunta)

        return Pair(preguntas, respuestas)
        /*preguntas.removeAtOrNull(contadorPregunta)
        respuestas.removeAtOrNull(contadorPregunta)

        return if (contadorPregunta > 0) {
            val validacionesGuiaModel =
                //setPintarTextosUseCase(true, preguntas, respuestas, contadorPregunta - 1, ruta)
                setPintarTextosUseCase(true, preguntas, respuestas, ruta)

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
        }*/
    }

    /*private fun ArrayList<String>.removeAtOrNull(index: Int) {
        if (index in indices) removeAt(index)
    }*/
}