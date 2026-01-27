package com.jonathanev.review.ui.mapper

import com.jonathanev.review.R
import com.jonathanev.review.data.mapper.toColorType
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.model.ScreenDataUi
import com.jonathanev.review.ui.model.ScreenDataNav

fun ScreenDataUi.toNav(): ScreenDataNav = ScreenDataNav(
    name = name,
    description = description,
    imgFolder = imgFolder.toInt(),
    color = color.toInt(),
    version = version
)

fun ScreenDataNav.toUi(): ScreenDataUi = ScreenDataUi(
    name = name,
    description = description,
    imgFolder = imgFolder.toIconType(),
    color = color.toColorType(),
    version = version
)

fun Int.toIconType(): IconType {
    return when(this) {
        R.drawable.ic_lightbulb_solid_full -> IconType.LIGHTBULB
        R.drawable.ic_anchor_solid_full -> IconType.ANCHOR_SOLID_FULL
        R.drawable.ic_angellist_brands_solid_full -> IconType.ANGELLIST_BRANDS_SOLID_FULL
        R.drawable.ic_bacteria_solid_full -> IconType.BACTERIA_SOLID_FULL
        else -> IconType.ANCHOR_SOLID_FULL
    }
}

fun IconType.toInt(): Int {
    return when(this){
        IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
        IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
        IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
        IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
    }
}

fun ColorType.toInt(): Int {
    return when(this){
        ColorType.Black -> R.color.black
        ColorType.Gray -> R.color.text_gray
        is ColorType.RandomColor -> this.color
        ColorType.White -> R.color.white
    }
}

fun IconType.toDrawableRes(): Int = when(this) {
    IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
    IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
    IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
    IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
}

fun ColorType.toColorRes(): Int = when(this) {
    ColorType.Black -> R.color.black
    ColorType.Gray -> R.color.text_gray
    ColorType.White -> R.color.white
    is ColorType.RandomColor -> color
}