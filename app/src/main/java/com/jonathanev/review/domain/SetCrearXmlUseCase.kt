package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.GuiaRepositoryImpl
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionItemDomain
import java.io.File
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(
        nameGuide: String,
        description: String,
        version: String = "2",
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean {
        return guiaRepository.saveFileV2(nameGuide, description, version, preguntas, respuestas)
    }
}