package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.presentation.model.QuestionContentUi

data class StepNavigationCarouselProv(
    val guideContext: GuideContext,
    val listQuestionContent: List<QuestionContentUi>
) {
    override fun toString(): String {
        return "Size_${listQuestionContent.size}_Mode_$guideContext"
    }
}

private val guideDomainModel = GuideDomainModel(GuideVersion.V2, "", "")
class StepNavigationCarouselProviders: PreviewParameterProvider<StepNavigationCarouselProv> {
    override val values: Sequence<StepNavigationCarouselProv>
        get() = sequenceOf(
            StepNavigationCarouselProv(
                guideContext = GuideContext.Creating(guideDomainModel),
                listQuestionContent =
                    listOf(
                        QuestionContentUi.Text("Hola", listOf()),
                        QuestionContentUi.Text("b", listOf()),
                        QuestionContentUi.Text("a", listOf())
                    )
            ),
            StepNavigationCarouselProv(
                guideContext = GuideContext.Editing(guideDomainModel, 0),
                listQuestionContent =
                    emptyList()
            ),
            StepNavigationCarouselProv(
                guideContext = GuideContext.Browsing(guideDomainModel, 0),
                listQuestionContent =
                    listOf(
                        QuestionContentUi.Text("Adios", listOf()),
                        QuestionContentUi.Text("b", listOf()),
                        QuestionContentUi.Text("a", listOf())
                    )
            )
        )
}