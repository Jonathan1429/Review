package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import javax.inject.Inject

class GetGuideXmlDataUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(guide: GuideDomainModel): GetGuideResult {
        return guiaRepository.getXMLGuide(GuideContext.Actual(guide))
    }
}