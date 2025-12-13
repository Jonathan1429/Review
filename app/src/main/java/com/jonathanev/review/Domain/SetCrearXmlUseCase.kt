package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(
        nameGuide: String,
        description: String,
        currentPath: String,
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>,
    ): Boolean {
        return guiaRepository.saveFileV2(nameGuide, description, currentPath, preguntas, respuestas)
    }
}