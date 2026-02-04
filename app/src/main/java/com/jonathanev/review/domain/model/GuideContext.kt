package com.jonathanev.review.domain.model

sealed class GuideContext {
    data class DeleteGuide(
        val guide: GuideDomainModel
    ) : GuideContext()

    data class Actual(
        val guide: GuideDomainModel
    ) : GuideContext()

    data class Moving(
        val guide: GuideDomainModel,
        val oldGuidePath: GuidePath,
        val oldImagePath: GuidePath
    ) : GuideContext()

    data class Rename(
        val guide: GuideDomainModel,
        val name: RequiredAttrGuide,
        val description: OptionalAttrGuide
    ) : GuideContext()
}