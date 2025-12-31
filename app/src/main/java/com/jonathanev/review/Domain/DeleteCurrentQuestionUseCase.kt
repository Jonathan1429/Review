package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.model.QuestionItemDomain
import javax.inject.Inject

class DeleteCurrentQuestionUseCase @Inject constructor() {
    operator fun invoke(
        preguntas: MutableList<QuestionItemDomain>,
        respuestas: MutableList<QuestionItemDomain>,
        contadorPregunta: Int,
    ) {
        if (contadorPregunta in preguntas.indices) preguntas.removeAt(contadorPregunta)
        if (contadorPregunta in respuestas.indices) respuestas.removeAt(contadorPregunta)

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
}