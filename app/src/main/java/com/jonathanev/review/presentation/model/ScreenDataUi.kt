package com.jonathanev.review.presentation.model

data class ScreenDataUi(
    val name: String,
    val description: String = "",
    val imgFolder: IconType,
    val color: ColorType,
    val version: Int = 1
)