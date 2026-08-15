package com.jonathanev.review.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class GuideContext : Parcelable {
    @Parcelize
    data class DeleteGuide(val guide: GuideDomainModel) : GuideContext(), Parcelable

    @Parcelize
    data class Browsing(val guide: GuideDomainModel, val position: Int) : GuideContext(), Parcelable

    @Parcelize
    data class Editing(val guide: GuideDomainModel, val position: Int) : GuideContext(), Parcelable

    @Parcelize
    data class Moving(
        val guide: GuideDomainModel,
        override val oldRelativeGuidePath: RelativeGuidePath
    ) : GuideContext(), HasOriginPath, Parcelable

    @Parcelize
    data class Rename(
        val guide: GuideDomainModel,
        val name: RequiredAttrGuide,
        val description: OptionalAttrGuide
    ) : GuideContext(), Parcelable

    @Parcelize
    data class Creating(val guide: GuideDomainModel) : GuideContext(), Parcelable
}