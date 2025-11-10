package com.jonathanev.review.Data.Model.prueba

sealed class QuestionContent {
    data object None : QuestionContent()
    data class Text(var text: String, val colorRanges:List<ColorRange>): QuestionContent()
    data class Image(val decodedPath: String, val encodedPath: String): QuestionContent()
}

data class ColorRange(
    val start: Int,
    val end: Int,
    val color: Int
)