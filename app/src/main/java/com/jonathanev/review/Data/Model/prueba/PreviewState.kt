package com.jonathanev.review.Data.Model.prueba

import android.graphics.Color

data class PreviewState(
    val icon: Int = 0,
    val color: Int = Color.BLACK,
    //val name: String = "",
    val selectedIndex: Int = -1,
    val icons: List<Int> = emptyList()
)