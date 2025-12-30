package com.jonathanev.review.Domain

import com.jonathanev.review.data.GuiaRepositoryImpl
import com.jonathanev.review.presentation.model.QuestionItem
import java.io.File
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepositoryImpl: GuiaRepositoryImpl
) {
    operator fun invoke(
        nameGuide: String,
        description: String,
        currentPath: File,
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>,
    ): Boolean {
        return guiaRepositoryImpl.saveFileV2(nameGuide, description, currentPath, preguntas, respuestas)
    }
}