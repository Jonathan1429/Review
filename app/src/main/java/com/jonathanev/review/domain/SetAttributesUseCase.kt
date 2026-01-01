package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.domain.model.QuestionItemDomain
import java.io.File
import javax.inject.Inject

class SetAttributesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(
        file: File,
        fileName: String,
        description: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): Boolean {
        return guiaRepository.setAttributesGuide(file, fileName, description, preguntas, respuestas)
    }
}