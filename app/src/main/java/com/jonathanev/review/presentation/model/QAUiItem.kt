package com.jonathanev.review.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class QuestionContentUi : Parcelable {
    @Parcelize
    data object None : QuestionContentUi()
    @Parcelize
    data class Text(val text: String, val colorRanges: List<ColorRangeUi>) : QuestionContentUi()
    @Parcelize
    data class Image(val uri: String, val nameFile: String) : QuestionContentUi()
}

@Parcelize
data class ColorRangeUi(val start: Int, val end: Int, val color: Int) : Parcelable

@Parcelize
data class QuestionItemUi(
    val content: List<QuestionContentUi>
) : Parcelable

/*@Parcelize
data class QAUiItem(
    val preguntas: List<QuestionItemUi>,
    val respuestas: List<QuestionItemUi>,
) : Parcelable*/