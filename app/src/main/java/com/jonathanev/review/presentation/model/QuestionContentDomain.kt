package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class QuestionContentDomain: Parcelable {
    @Parcelize
    data object None : QuestionContentDomain()
    @Parcelize
    data class Text(val text: String, val colorRangeDomains:List<ColorRangeDomain>): QuestionContentDomain()
    @Parcelize
    data class Image(val uri: String, val nameFile: String): QuestionContentDomain()
}

@Parcelize
data class ColorRangeDomain(
    val start: Int,
    val end: Int,
    val color: Int
): Parcelable