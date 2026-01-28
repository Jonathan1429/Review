package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import javax.inject.Inject

class GetGuideXmlDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(context: GuideContext): GetGuideResult {
        return guiaRepository.getXMLGuide(context)
    }
}