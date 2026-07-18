package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideUiModel

class ItemGuideProvider() : PreviewParameterProvider<GuideUiModel> {
    override val values: Sequence<GuideUiModel>
        get() = sequenceOf(
            GuideUiModel("Kotlin", "Sintaxis basica de Kotlin"),
            GuideUiModel("Test", "Test unitarios")
        )
}