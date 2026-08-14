package com.jonathanev.review.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
enum class GuideVersion : Parcelable {
    V1, V2
}
