package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class QuestionItem(
    val id: Int,
    val text: String,
    val docsCount: Int,
    val imgsCount: Int,
)

class QuestionItemProvider(): PreviewParameterProvider<QuestionItem> {
    override val values: Sequence<QuestionItem>
        get() = sequenceOf(
            QuestionItem(
                id = 1,
                text = "¿Esta es la pregunta 1?",
                docsCount = 2,
                imgsCount = 1
            ),
            QuestionItem(
                id = 2,
                text = "¿Esta es la pregunta 2?",
                docsCount = 9,
                imgsCount = 4
            )
        )
}