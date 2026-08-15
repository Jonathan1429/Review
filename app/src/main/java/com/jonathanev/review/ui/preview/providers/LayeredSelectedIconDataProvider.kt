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

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Anchor_no_seleccionado"
            1 -> "Anchor_seleccionado"
            2 -> "Bacteria_no_seleccionada"
            3 -> "Bacteria_seleccionada"
            4 -> "Angellist_no_seleccionado"
            5 -> "Angelist_seleccionado"
            6 -> "Lightbulb_no_seleccionado"
            7 -> "Lightbulb_seleccionado"
            else -> super.getDisplayName(index)
        }
    }
}