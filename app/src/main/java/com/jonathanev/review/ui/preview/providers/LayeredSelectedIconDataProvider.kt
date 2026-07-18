package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.IconType

data class IconSelected(
    val icon: IconType,
    val isSelected: Boolean
)

class LayeredSelectedIconDataProvider: PreviewParameterProvider<IconSelected>{
    override val values: Sequence<IconSelected>
        get() = sequenceOf(
            IconSelected(
                icon = IconType.ANCHOR_SOLID_FULL,
                isSelected = false
            ),
            IconSelected(
                icon = IconType.ANCHOR_SOLID_FULL,
                isSelected = true
            ),
            IconSelected(
                icon = IconType.BACTERIA_SOLID_FULL,
                isSelected = false
            ),
            IconSelected(
                icon = IconType.BACTERIA_SOLID_FULL,
                isSelected = true
            ),
            IconSelected(
                icon = IconType.ANGELLIST_BRANDS_SOLID_FULL,
                isSelected = false
            ),
            IconSelected(
                icon = IconType.ANGELLIST_BRANDS_SOLID_FULL,
                isSelected = true
            ),
            IconSelected(
                icon = IconType.LIGHTBULB,
                isSelected = false
            ),
            IconSelected(
                icon = IconType.LIGHTBULB,
                isSelected = true
            )
        )
}