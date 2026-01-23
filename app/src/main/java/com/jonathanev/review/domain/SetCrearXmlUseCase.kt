package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.service.FileNamingRules
import javax.inject.Inject

class SetCrearXmlUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository,
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
        if (!path){
            return false
        }
        val file = FileNamingRules.buildXmlFileName(nameGuide)
        return guiaRepository.saveGuide(nameGuide, file, description, version, preguntas, respuestas, navigationPathRepository.currentPathGuides.value)
    }
}