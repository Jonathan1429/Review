package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.IconType

data class PropertiesFolderSelected(
    val listIcons: List<IconType>,
    val posSelected: Int
)

class IconsDataProvider : PreviewParameterProvider<PropertiesFolderSelected> {
    override val values: Sequence<PropertiesFolderSelected> = sequenceOf(
        PropertiesFolderSelected(
            listIcons = listOf(IconType.LIGHTBULB),
            posSelected = 0
        ),
        PropertiesFolderSelected(
            listIcons = listOf(
                IconType.ANCHOR_SOLID_FULL,
                IconType.ANGELLIST_BRANDS_SOLID_FULL,
                IconType.BACTERIA_SOLID_FULL
            ),
            posSelected = 0
        ),
        PropertiesFolderSelected(
            listIcons = listOf(
                IconType.ANCHOR_SOLID_FULL,
                IconType.ANGELLIST_BRANDS_SOLID_FULL,
                IconType.BACTERIA_SOLID_FULL
            ),
            posSelected = 1
        ),
        PropertiesFolderSelected(
            listIcons = listOf(
                IconType.ANCHOR_SOLID_FULL,
                IconType.ANGELLIST_BRANDS_SOLID_FULL,
                IconType.BACTERIA_SOLID_FULL
            ),
            posSelected = 2
        )
    )

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Icons_file_pos_$index"
            1 -> "Icons_folder_pos_$index"
            2 -> "Icons_folder_pos_$index"
            3 -> "Icons_folder_pos_$index"
            else -> super.getDisplayName(index)
        }
    }
}