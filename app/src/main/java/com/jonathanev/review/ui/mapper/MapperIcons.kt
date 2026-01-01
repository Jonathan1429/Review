package com.jonathanev.review.ui.mapper

import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.IconType

fun IconType.toInt(): Int{
    return when(this){
        IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
        IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
        IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
        IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
    }
}