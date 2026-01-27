package com.jonathanev.review.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenDataNav(
    val name: String,
    val description: String = "",
    val imgFolder: Int,
    val color: Int,
    val version: Int = 1
) : Parcelable