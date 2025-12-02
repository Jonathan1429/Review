package com.jonathanev.review.Data.Model.prueba

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class QuestionContent: Parcelable {
    @Parcelize
    data object None : QuestionContent()
    @Parcelize
    data class Text(val text: String, val colorRanges:List<ColorRange>): QuestionContent()
    @Parcelize
    data class Image(val decodedPath: String, val encodedPath: String): QuestionContent()
}

@Parcelize
data class ColorRange(
    val start: Int,
    val end: Int,
    val color: Int
): Parcelable