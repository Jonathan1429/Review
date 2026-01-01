package com.jonathanev.review.data.model

data class QAItemXml(
    val question: ResponseXml = ResponseXml.Empty,
    val answer: ResponseXml = ResponseXml.Empty
)

sealed class ResponseXml {
    data object Empty : ResponseXml()
    data class Filled(val item: QuestionItemXml) : ResponseXml()
}

data class QuestionItemXml(
    val content: List<QuestionContentXml>
)

sealed class QuestionContentXml {
    data object None : QuestionContentXml()
    data class Text(val text: String, val colorRangeXml:List<ColorRangeXml>): QuestionContentXml()
    data class Image(val uri: String, val nameFile: String): QuestionContentXml()
}

data class ColorRangeXml(
    val start: Int,
    val end: Int,
    val color: Int
)