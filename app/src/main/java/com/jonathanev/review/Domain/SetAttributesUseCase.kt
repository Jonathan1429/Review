package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.presentation.model.QuestionItem
import java.io.File
import javax.inject.Inject

class SetAttributesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(
        file: File,
        fileName: String,
        description: String,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>
    ): Boolean {
        return guiaRepository.setAttributesGuide(file, fileName, description, preguntas, respuestas)
    }
}