package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.data.mapper.GuideXmlMapper
import java.io.File
import javax.inject.Inject

class GetAttributesGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(file: File): GuideDomainModel {
        val guideXmlModel = guiaRepository.getAttributesGuide(file)
        val guideDomain = GuideXmlMapper.toDomain(guideXmlModel)
        return guideDomain
    }
}