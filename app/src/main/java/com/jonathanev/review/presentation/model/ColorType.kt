package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ColorType : Parcelable {
    @Parcelize
    data object Black : ColorType(), Parcelable

    @Parcelize
    data object Gray : ColorType(), Parcelable

    @Parcelize
    data object White : ColorType(), Parcelable

    @Parcelize
    data object Default : ColorType(), Parcelable

    @Parcelize
    data class RandomColor(val color: Int) : ColorType(), Parcelable
}