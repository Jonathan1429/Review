package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(
        currentPath: String,
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>,
        /*didTheGuideAlreadyExist: Boolean,
        ruta: String*/
    ): Boolean {
        return guiaRepository.saveFileV2(currentPath, preguntas, respuestas)
    }
}