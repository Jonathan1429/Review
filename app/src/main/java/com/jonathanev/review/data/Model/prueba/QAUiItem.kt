package com.jonathanev.review.data.Model.prueba

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QAUiItem(
    val preguntas: List<QuestionUiItem>,
    val respuestas: List<QuestionUiItem>,
) : Parcelable

@Parcelize
data class QuestionUiItem(
    val content: List<QuestionContentUi>
) : Parcelable

@Parcelize
sealed class QuestionContentUi : Parcelable {
    @Parcelize
    data object None : QuestionContentUi()

    @Parcelize
    data class Text(val text: String, val colorRanges: List<ColorRangeUi>) : QuestionContentUi()

    @Parcelize
    data class Image(val decodedPath: String, val encodedPath: String) : QuestionContentUi()
}



// Si ColorRange también existe:
@Parcelize
data class ColorRangeUi(val start: Int, val end: Int, val color: Int) : Parcelable