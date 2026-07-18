package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideUiModel

class ItemGuideProvider() : PreviewParameterProvider<GuideUiModel> {
    override val values: Sequence<GuideUiModel>
        get() = sequenceOf(
            GuideUiModel("Kotlin", "Sintaxis basica de Kotlin"),
            GuideUiModel("Test", "Test unitarios")
        )

    override fun getDisplayName(index: Int): String? {
        return when(index)  {
            0 -> "Guia_Kotlin"
            1 -> "Guia_Test"
            else -> super.getDisplayName(index)
        }
    }
}