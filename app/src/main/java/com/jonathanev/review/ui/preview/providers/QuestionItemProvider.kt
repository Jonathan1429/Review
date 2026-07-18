package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi

data class QuestionItemProv(
    val question: String,
    val noTexts: String,
    val noImages: String
)

class QuestionItemProvider() : PreviewParameterProvider<QuestionItemProv> {
    override val values: Sequence<QuestionItemProv>
        get() = sequenceOf(
            QuestionItemProv(
                question = "¿Como se crea un test unitario?",
                noTexts = "2",
                noImages = "3"
            ),
            QuestionItemProv(
                question = "¿Que significa una variable mutable?",
                noTexts = "2",
                noImages = "4"
            )
        )
}