package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
sealed class ColorType : Parcelable {
    @Serializable
    @Parcelize
    data object Black : ColorType(), Parcelable

    @Serializable
    @Parcelize
    data object Gray : ColorType(), Parcelable

    @Serializable
    @Parcelize
    data object White : ColorType(), Parcelable

    @Serializable
    @Parcelize
    data object Default : ColorType(), Parcelable

    @Serializable
    @Parcelize
    data class RandomColor(val color: Int) : ColorType(), Parcelable
}
