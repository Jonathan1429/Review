package com.jonathanev.review.ui.mapper

import android.graphics.Color
import com.jonathanev.review.presentation.model.ColorType

fun ColorType.toInt(): Int {
    return when(this){
        ColorType.Black -> Color.BLACK
        ColorType.Gray -> Color.GRAY
        is ColorType.RandomColor -> this.color
    }
}