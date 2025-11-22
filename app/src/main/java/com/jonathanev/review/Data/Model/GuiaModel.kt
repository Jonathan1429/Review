package com.jonathanev.review.Data.Model

import android.graphics.Color
import com.jonathanev.review.R

data class GuiaModel(
    val nombreGuia: String,
    val description: String = "",
    val imgGuia: Int = R.drawable.ic_anchor_solid_full,
    val color: Int = Color.BLACK,
    val carpeta: Boolean = false,

    val num: Int = 0
)