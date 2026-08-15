package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion

data class CustomTopBarProv(
    val actualQuestion: Int,
    val totalQuestion: Int,
    val guideContext: GuideContext
)

private val guideDomainModel = GuideDomainModel(GuideVersion.V2, "", "")
class CustomTopBarProvider: PreviewParameterProvider<CustomTopBarProv> {
    override val values: Sequence<CustomTopBarProv>
        get() = sequenceOf(
            CustomTopBarProv(
                actualQuestion = 2,
                totalQuestion = 7,
                guideContext = GuideContext.Browsing(guideDomainModel, 0)
            ),
            CustomTopBarProv(
                actualQuestion = 2,
                totalQuestion = 7,
                guideContext = GuideContext.Editing(guideDomainModel, 0)
            ),
            CustomTopBarProv(
                actualQuestion = 2,
                totalQuestion = 7,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
        )

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Review"
            1 -> "Edit"
            2 -> "Create"
            else -> super.getDisplayName(index)
        }
    }
}