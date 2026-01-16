package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.data.repository.NavigationPathRepository
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(
        nameGuide: String,
        description: String,
        version: GuideVersion = GuideVersion.V2,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
    ): Boolean {
        return guiaRepository.saveGuide(nameGuide, description, version, preguntas, respuestas, navigationPathRepository.currentPathGuides)
    }
}