package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class PreviewQuestionsProvider() : PreviewParameterProvider<List<QuestionItem>> {
    override val values: Sequence<List<QuestionItem>>
        get() = sequenceOf(
            listOf(
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
        )
}