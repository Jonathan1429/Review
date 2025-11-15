package com.jonathanev.review.UI.Utils

import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.ColorRangeUi
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionContentUi
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import com.jonathanev.review.Data.Model.prueba.QuestionUiItem

fun QuestionItem.toUi(): QuestionUiItem {
    return QuestionUiItem(
        content = content.map { it.toUi() }
    )
}

fun QuestionUiItem.toDomain(): QuestionItem {
    return QuestionItem(
        content = content.map { it.toDomain() }
    )
}

fun QuestionContent.toUi(): QuestionContentUi {
    return when (this) {
        is QuestionContent.None -> QuestionContentUi.None
        is QuestionContent.Text -> QuestionContentUi.Text(
            text = this.text,
            colorRanges = this.colorRanges.map { it.toUi() }
        )

        is QuestionContent.Image -> QuestionContentUi.Image(
            decodedPath = this.decodedPath,
            encodedPath = this.encodedPath
        )
    }
}

fun QuestionContentUi.toDomain(): QuestionContent {
    return when (this) {
        is QuestionContentUi.None -> QuestionContent.None
        is QuestionContentUi.Text -> QuestionContent.Text(
            text = this.text,
            colorRanges = this.colorRanges.map { it.toDomain() }
        )

        is QuestionContentUi.Image -> QuestionContent.Image(
            decodedPath = this.decodedPath,
            encodedPath = this.encodedPath
        )
    }
}

fun ColorRange.toUi(): ColorRangeUi {
    return ColorRangeUi(start = start, end = end, color = color)
}

fun ColorRangeUi.toDomain(): ColorRange {
    return ColorRange(start = start, end = end, color = color)
}