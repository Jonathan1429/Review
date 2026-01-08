package com.jonathanev.review.domain.model

import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType

data class FolderAttributesDomain(
    val name: String,
    val imgFolder: IconType,
    val color: ColorType,
)