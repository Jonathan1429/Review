package com.jonathanev.review.ui.mapper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.model.ContentType

/*fun ScreenDataUi.toNav(): PropertiesGuide = PropertiesGuide(
    name = name,
    description = description
)*/

/*fun PropertiesGuide.toUi(): ScreenDataUi = ScreenDataUi(
    name = name,
    description = description,
    imgFolder = imgFolder.toIconType(),
    color = color.toColorType(),
    version = version
)*/

fun Int.toIconType(): IconType {
    return when (this) {
        R.drawable.ic_lightbulb_solid_full -> IconType.LIGHTBULB
        R.drawable.ic_anchor_solid_full -> IconType.ANCHOR_SOLID_FULL
        R.drawable.ic_angellist_brands_solid_full -> IconType.ANGELLIST_BRANDS_SOLID_FULL
        R.drawable.ic_bacteria_solid_full -> IconType.BACTERIA_SOLID_FULL
        else -> IconType.ANCHOR_SOLID_FULL
    }
}

fun IconType.toInt(): Int {
    return when (this) {
        IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
        IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
        IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
        IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
    }
}

fun ColorType.toInt(isDark: Boolean): Int {
    return when (this) {
        ColorType.Black -> Color.Black.toArgb()
        ColorType.Gray -> Color.Gray.toArgb()
        is ColorType.RandomColor -> this.color
        ColorType.White -> Color.White.toArgb()
        ColorType.Default -> if (isDark) Color.White.toArgb() else Color.Black.toArgb()
    }
}

fun IconType.toDrawableRes(): Int = when (this) {
    IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
    IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
    IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
    IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
}

fun ContentType.toDrawable(): Int = when(this){
    ContentType.TEXT -> R.drawable.ic_file
    ContentType.IMAGE -> R.drawable.ic_image
}