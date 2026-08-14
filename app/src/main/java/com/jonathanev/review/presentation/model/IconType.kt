package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
enum class IconType : Parcelable {
    LIGHTBULB,
    ANCHOR_SOLID_FULL,
    ANGELLIST_BRANDS_SOLID_FULL,
    BACTERIA_SOLID_FULL
}
