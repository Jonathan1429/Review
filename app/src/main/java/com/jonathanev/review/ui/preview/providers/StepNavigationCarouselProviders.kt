package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideMode

class StepNavigationCarouselProviders: PreviewParameterProvider<GuideMode> {
    override val values: Sequence<GuideMode>
        get() = sequenceOf(
            GuideMode.Create("", ""),
            GuideMode.Edit("", "", 0),
            GuideMode.Review("", 0)
        )
}