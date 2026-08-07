package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import javax.inject.Inject

class GetGuideXmlDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    suspend operator fun invoke(context: GuideContext): GetGuideResult {
        return when(context){
            is GuideContext.Creating -> GetGuideResult.Success(context.guide, emptyList())
            is GuideContext.Browsing -> guiaRepository.getXMLGuide(context.guide)
            is GuideContext.Editing -> guiaRepository.getXMLGuide(context.guide)
            is GuideContext.Moving -> guiaRepository.getXMLGuide(context.guide)
            else -> GetGuideResult.NotFound
        }
    }
}