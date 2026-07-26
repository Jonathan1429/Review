package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class FolderAttributesUi(
    val name: String,
    val imgFolder: IconType,
    val color: ColorType,
)