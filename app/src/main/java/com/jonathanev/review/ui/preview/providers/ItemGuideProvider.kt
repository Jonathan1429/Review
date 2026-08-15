package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.model.GuideVersion

class ItemGuideProvider() : PreviewParameterProvider<GuideUiModel> {
    override val values: Sequence<GuideUiModel>
        get() = sequenceOf(
            GuideUiModel(GuideVersion.V2, "Kotlin", "Sintaxis basica de Kotlin"),
            GuideUiModel(GuideVersion.V2, "Test", "Test unitarios")
        )

    override fun getDisplayName(index: Int): String? {
        return when(index)  {
            0 -> "Guia_Kotlin"
            1 -> "Guia_Test"
            else -> super.getDisplayName(index)
        }
    }
}