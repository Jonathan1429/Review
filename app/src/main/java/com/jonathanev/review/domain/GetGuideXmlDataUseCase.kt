package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.GetGuideResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetGuideXmlDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(context: GuideContext): GetGuideResult {
        val relativeGuidePath = navigationPathRepository.relativePath.first()

        return when(context){
            is GuideContext.Browsing -> guiaRepository.getXMLGuide(context.guide)
            is GuideContext.Editing -> guiaRepository.getXMLGuide(context.guide)
            is GuideContext.Moving -> guiaRepository.getXMLGuide(context.guide)
            else -> GetGuideResult.NotFound
        }
        //return guiaRepository.getXMLGuide(context)
    }
}