package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepositoryImpl
import com.jonathanev.review.domain.model.QuestionItemDomain
import java.io.File
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepositoryImpl: GuiaRepositoryImpl
) {
    operator fun invoke(
        nameGuide: String,
        description: String,
        currentPath: File,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean {
        return guiaRepositoryImpl.saveFileV2(nameGuide, description, currentPath, preguntas, respuestas)
    }
}