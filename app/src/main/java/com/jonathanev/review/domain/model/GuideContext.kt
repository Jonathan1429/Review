package com.jonathanev.review.domain.model

sealed class GuideContext {
    data class DeleteGuide(val guide: GuideDomainModel) : GuideContext()

    data class Browsing(val guide: GuideDomainModel) : GuideContext()

    data class Editing(val guide: GuideDomainModel) : GuideContext()

    data class Moving(
        val guide: GuideDomainModel,
        override val oldRelativeGuidePath: RelativeGuidePath
    ) : GuideContext(), HasOriginPath

    data class Rename(
        val guide: GuideDomainModel,
        val name: RequiredAttrGuide,
        val description: OptionalAttrGuide
    ) : GuideContext()
}