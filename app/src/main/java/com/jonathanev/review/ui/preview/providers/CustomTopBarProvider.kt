package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideMode

data class CustomTopBarProv(
    val actualQuestion: Int,
    val totalQuestion: Int,
    val guideMode: GuideMode
)

class CustomTopBarProvider: PreviewParameterProvider<CustomTopBarProv> {
    override val values: Sequence<CustomTopBarProv>
        get() = sequenceOf(
            CustomTopBarProv(
                actualQuestion = 2,
                totalQuestion = 7,
                guideMode = GuideMode.Review
            ),
            CustomTopBarProv(
                actualQuestion = 2,
                totalQuestion = 7,
                guideMode = GuideMode.Edit
            ),
            CustomTopBarProv(
                actualQuestion = 2,
                totalQuestion = 7,
                guideMode = GuideMode.Create
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