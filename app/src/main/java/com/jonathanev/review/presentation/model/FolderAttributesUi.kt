package com.jonathanev.review.presentation.model

import androidx.annotation.DrawableRes

data class FolderAttributesUi(
    val name: String,
    @DrawableRes val imgFolder: Int,
    val color: Int,
)