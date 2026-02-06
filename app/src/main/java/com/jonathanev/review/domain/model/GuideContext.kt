package com.jonathanev.review.domain.model

sealed class GuideContext {
    data class DeleteGuide(
        val guide: GuideDomainModel,
        val relativeGuidePath: RelativeGuidePath
    ) : GuideContext()

    data class Browsing(
        val guide: GuideDomainModel,
        val relativeGuidePath: RelativeGuidePath
    ): GuideContext()

    data class Editing(
        val guide: GuideDomainModel,
        val relativeGuidePath: RelativeGuidePath
    ) : GuideContext()

    data class Moving(
        val guide: GuideDomainModel,
        val oldRelativeGuidePath: RelativeGuidePath,
        val relativeGuidePath: RelativeGuidePath
    ) : GuideContext()

    data class Rename(
        val guide: GuideDomainModel,
        val relativeGuidePath: RelativeGuidePath,
        val name: RequiredAttrGuide,
        val description: OptionalAttrGuide
    ) : GuideContext()
}