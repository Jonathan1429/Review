package com.jonathanev.review.data

import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType

data class AttributesFolderModel(
    val name: String,
    val imgFolder: IconType,
    val color: ColorType
)