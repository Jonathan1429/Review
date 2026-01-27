package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val directoryManager: DirectoryManager
) {
    operator fun invoke(
        nameGuide: String,
        description: String,
        version: GuideVersion = GuideVersion.V2,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean {
        val path = directoryManager.createPathGuide(nameGuide)
        if (!path) {
            return false
        }
        return guiaRepository.saveGuide(
            GuideDomainModel(version, nameGuide, description),
            preguntas,
            respuestas,
        )
    }
}