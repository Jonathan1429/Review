package com.jonathanev.review.presentation.model

import kotlinx.serialization.Serializable

@Serializable
sealed class QuestionContentUi {
    @Serializable
    data object None : QuestionContentUi()

    @Serializable
    data class Text(val text: String, val colorRanges: List<ColorRangeUi>) : QuestionContentUi()

    @Serializable
    data class Image(val uri: String, val nameFile: String) : QuestionContentUi()
}

@Serializable
data class ColorRangeUi(val start: Int, val end: Int, val color: Int)

@Serializable
data class QuestionItemUi(
    val content: List<QuestionContentUi>
)

/*@Parcelize
data class QAUiItem(
    val preguntas: List<QuestionItemUi>,
    val respuestas: List<QuestionItemUi>,
) : Parcelable*/