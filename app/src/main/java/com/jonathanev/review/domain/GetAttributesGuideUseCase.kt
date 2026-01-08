package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.mapper.GuideXmlMapper
import com.jonathanev.review.domain.repository.PathProvider
import java.io.File
import javax.inject.Inject

class GetAttributesGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
) {
    operator fun invoke() {
        /*val guideXmlModel = guiaRepository.getAttributesGuide()
        val guideDomain = GuideXmlMapper.toDomain(guideXmlModel)
        return guideDomain*/
    }
}