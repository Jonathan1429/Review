package com.jonathanev.review.presentation.mapper

import com.jonathanev.review.data.serialization.IconKeys
import com.jonathanev.review.presentation.model.IconType

fun IconType.toIconKeys(): String {
    return when (this) {
        IconType.LIGHTBULB -> IconKeys.LIGHTBULB
        IconType.ANGELLIST_BRANDS_SOLID_FULL -> IconKeys.ANGELLIST_BRANDS_SOLID_FULL
        IconType.BACTERIA_SOLID_FULL -> IconKeys.BACTERIA_SOLID_FULL
        else -> IconKeys.ANCHOR_SOLID_FULL
    }
}