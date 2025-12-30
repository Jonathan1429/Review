package com.jonathanev.review.presentation.state

import android.graphics.Color

data class PreviewState(
    val icon: Int = 0,
    val color: Int = Color.BLACK,
    val selectedIndex: Int = -1,
    val icons: List<Int> = emptyList()
)