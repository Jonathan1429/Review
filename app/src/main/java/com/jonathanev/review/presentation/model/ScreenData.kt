package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ScreenData(
    val name: String,
    val description: String = "",
    val imgFolder: IconType,
    val color: Int,
    val version: Int = 1
): Parcelable
