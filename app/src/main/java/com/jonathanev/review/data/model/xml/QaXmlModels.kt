package com.jonathanev.review.data.model.xml

data class QAItemXmlDto(
    val question: ResponseXmlDto = ResponseXmlDto.Empty,
    val answer: ResponseXmlDto = ResponseXmlDto.Empty
)

sealed class ResponseXmlDto {
    data object Empty : ResponseXmlDto()
    data class Filled(val item: QuestionItemXmlDto) : ResponseXmlDto()
}

data class QuestionItemXmlDto(
    val content: List<QuestionContentXmlDto>
)

sealed class QuestionContentXmlDto {
    data class Text(val text: String, val colorRangeXmlDto:List<ColorRangeXmlDto>): QuestionContentXmlDto()
    data class Image(val uri: String, val nameFile: String): QuestionContentXmlDto()
}

data class ColorRangeXmlDto(
    val start: Int,
    val end: Int,
    val color: Int
)