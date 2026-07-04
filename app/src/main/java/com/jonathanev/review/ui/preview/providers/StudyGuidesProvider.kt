package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideUiModel

class StudyGuidesProvider() : PreviewParameterProvider<List<GuideUiModel>> {
    override val values: Sequence<List<GuideUiModel>>
        get() = sequenceOf(
            listOf(
                GuideUiModel("Kotlin", "Sintaxis basica de Kotlin"),
                GuideUiModel("Test", "Test unitarios")
            )
        )
}