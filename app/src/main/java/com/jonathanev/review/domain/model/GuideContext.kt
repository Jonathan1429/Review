package com.jonathanev.review.domain.model

sealed class GuideContext {
    data class Browsing(
        val currentPath: GuidePath
    ) : GuideContext()

    data class Actual(
        val guide: GuideDomainModel,
        val currentGuidePath: GuidePath
    ) : GuideContext()

    data class Moving(
        val guide: GuideDomainModel,
        val oldGuidePath: GuidePath,
        val currentGuidePath: GuidePath,
    ) : GuideContext()

    data class Rename(
        val guide: GuideDomainModel,
        val oldGuidePath: GuidePath,
        val currentGuidePath: GuidePath,
    ) : GuideContext()
}