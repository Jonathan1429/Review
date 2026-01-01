package com.jonathanev.review.data.mapper

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.data.model.GuideXmlModel

object GuideXmlMapper {
    fun toDomain(model: GuideXmlModel): GuideDomainModel =
        GuideDomainModel(
            nameGuide = model.nameGuide,
            description = model.description
        )
}