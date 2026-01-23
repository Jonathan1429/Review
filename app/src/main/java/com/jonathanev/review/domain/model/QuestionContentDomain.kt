package com.jonathanev.review.domain.model

sealed class QuestionContentDomain {
    data object None : QuestionContentDomain()
    data class Text(val text: String, val colorRangeDomains: List<ColorRangeDomain>) :
        QuestionContentDomain()

    data class Image(val uri: String, val nameFile: String) : QuestionContentDomain()
}

data class ColorRangeDomain(
    val start: Int,
    val end: Int,
    val color: Int
)