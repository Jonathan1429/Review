package com.jonathanev.review.presentation.model

/*enum class ColorType {
    BLACK,
    GRAY
}*/

sealed class ColorType{
    data object Black: ColorType()
    data object Gray: ColorType()
    data class RandomColor(val color: Int): ColorType()
}