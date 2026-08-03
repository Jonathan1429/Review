package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ColorType {
    data object Black: ColorType()
    data object Gray: ColorType()
    data object White: ColorType()
    data object Default: ColorType()
    data class RandomColor(val color: Int): ColorType()
}