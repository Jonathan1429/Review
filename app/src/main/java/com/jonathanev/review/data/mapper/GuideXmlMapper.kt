package com.jonathanev.review.data.mapper

import com.jonathanev.review.Domain.model.GuideDomainModel
import com.jonathanev.review.data.model.GuideXmlModel
import com.jonathanev.review.presentation.model.GuideUiModel

object GuideXmlMapper {
    fun toUi(model: GuideXmlModel): GuideUiModel =
        GuideUiModel(
            nameGuide = model.nameGuide,
            description = model.description
        )

    fun toDomain(model: GuideXmlModel): GuideDomainModel =
        GuideDomainModel(
            nameGuide = model.nameGuide,
            description = model.description
        )
}