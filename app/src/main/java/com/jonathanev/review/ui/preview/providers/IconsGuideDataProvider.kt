package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.IconType

class IconsGuideDataProvider : PreviewParameterProvider<List<IconType>> {
    override val values: Sequence<List<IconType>> = sequenceOf(
        listOf(IconType.LIGHTBULB)
    )
}