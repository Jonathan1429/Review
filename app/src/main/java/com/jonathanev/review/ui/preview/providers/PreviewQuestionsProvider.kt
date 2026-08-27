package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.PreviewQuestionUi
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi

class PreviewQuestionsProvider() : PreviewParameterProvider<PreviewQuestionStateUi> {
    override val values: Sequence<PreviewQuestionStateUi>
        get() = sequenceOf(
            PreviewQuestionStateUi(
                previewState = listOf(
                    PreviewQuestionUi(
                        id = "1",
                        question = QuestionContentUi.Text("¿Como se crea un test unitario?", emptyList()),
                        noTexts = "2",
                        noImages = "3"
                    ),
                    PreviewQuestionUi(
                        id = "2",
                        question = QuestionContentUi.Text("¿Que significa una variable mutable?", emptyList()),
                        noTexts = "2",
                        noImages = "4"
                    )
                )
            )
        )
}