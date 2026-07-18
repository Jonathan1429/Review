package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi

data class StepNavigationCarouselProv(
    val mode: GuideMode,
    val listQuestionContent: List<QuestionContentUi>
) {
    override fun toString(): String {
        return "Size_${listQuestionContent.size}_Mode_$mode"
    }
}

class StepNavigationCarouselProviders: PreviewParameterProvider<StepNavigationCarouselProv> {
    override val values: Sequence<StepNavigationCarouselProv>
        get() = sequenceOf(
            StepNavigationCarouselProv(
                mode = GuideMode.Create("", ""),
                listQuestionContent =
                    listOf(
                        QuestionContentUi.Text("Hola", listOf()),
                        QuestionContentUi.Text("b", listOf()),
                        QuestionContentUi.Text("a", listOf())
                    )
            ),
            StepNavigationCarouselProv(
                mode = GuideMode.Edit("", "", 0),
                listQuestionContent =
                    emptyList()
            ),
            StepNavigationCarouselProv(
                mode = GuideMode.Review("", 0),
                listQuestionContent =
                    listOf(
                        QuestionContentUi.Text("Adios", listOf()),
                        QuestionContentUi.Text("b", listOf()),
                        QuestionContentUi.Text("a", listOf())
                    )
            )
        )
}