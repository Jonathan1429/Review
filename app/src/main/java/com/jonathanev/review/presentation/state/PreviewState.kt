package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType

data class PreviewState(
    val icon: IconType = IconType.ANCHOR_SOLID_FULL,
    val color: ColorType = ColorType.Black,
    val selectedIndex: Int = -1,
    val icons: List<IconType> = emptyList()
)