package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FolderAttributesUi(
    val name: String,
    val imgFolder: IconType,
    val color: ColorType,
) : Parcelable