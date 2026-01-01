package com.jonathanev.review.presentation.model

sealed class ColorType{
    data object Black: ColorType()
    data object Gray: ColorType()
    data object White: ColorType()
    data class RandomColor(val color: Int): ColorType()
}