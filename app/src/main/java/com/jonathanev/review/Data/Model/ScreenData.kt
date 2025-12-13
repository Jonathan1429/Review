package com.jonathanev.review.Data.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenData(
    val name: String,
    val description: String = "",
    val icon: Int? = null,
    val color: Int? = null
): Parcelable
