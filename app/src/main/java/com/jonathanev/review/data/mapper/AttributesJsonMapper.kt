package com.jonathanev.review.data.mapper

import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType

fun String.toIconType(): IconType {
    return when (this) {
        "ANGELLIST_BRANDS_SOLID_FULL" -> IconType.ANGELLIST_BRANDS_SOLID_FULL
        "LIGHTBULB" -> IconType.LIGHTBULB
        "ANCHOR_SOLID_FULL" -> IconType.ANCHOR_SOLID_FULL
        "BACTERIA_SOLID_FULL" -> IconType.BACTERIA_SOLID_FULL
        else -> IconType.ANCHOR_SOLID_FULL
    }
}

fun Int.toColorType(): ColorType {
    return when (this) {
        0 -> ColorType.Black
        else -> ColorType.RandomColor(this)
    }
}