package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepositoryImpl
import com.jonathanev.review.Data.Model.prueba.QuestionItem
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