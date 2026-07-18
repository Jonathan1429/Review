package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.IconType

data class PropertiesItemFolder(
    val iconRes: IconType
)

class BoxItemFolderDataProvider : PreviewParameterProvider<PropertiesItemFolder> {
    override val values: Sequence<PropertiesItemFolder> = sequenceOf(
        PropertiesItemFolder(iconRes = IconType.ANCHOR_SOLID_FULL),
        PropertiesItemFolder(iconRes = IconType.BACTERIA_SOLID_FULL),
        PropertiesItemFolder(iconRes = IconType.ANGELLIST_BRANDS_SOLID_FULL)
    )

    override fun getDisplayName(index: Int): String? {
        return when(index){
            0 -> "Anchor_solid_full"
            1 -> "Bacteria_solid_full"
            2 -> "Angellist_brands_solid_full"
            else -> super.getDisplayName(index)
        }
    }
}