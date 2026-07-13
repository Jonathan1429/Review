package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.FolderAction
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.state.PropertiesFilesState

data class PropertiesCreateFilesScreen(
    val listIcons: List<IconType>,
    val state: PropertiesFilesState,
    val fileFormMode: FileFormMode
)

class CreateFilesScreenDataProvider : PreviewParameterProvider<PropertiesCreateFilesScreen> {
    override val values: Sequence<PropertiesCreateFilesScreen>
        get() = sequenceOf(
            PropertiesCreateFilesScreen(
                listIcons = listOf(
                    IconType.ANCHOR_SOLID_FULL,
                    IconType.ANGELLIST_BRANDS_SOLID_FULL,
                    IconType.BACTERIA_SOLID_FULL
                ),
                state = PropertiesFilesState(
                    name = "",
                    description = "",
                    icon = IconType.BACTERIA_SOLID_FULL,
                    color = ColorType.White,
                    selectedIndex = 1,
                    icons = listOf(
                        IconType.ANCHOR_SOLID_FULL,
                        IconType.ANGELLIST_BRANDS_SOLID_FULL,
                        IconType.BACTERIA_SOLID_FULL
                    ),
                    showOverwriteDialogFile = false
                ),
                fileFormMode = FileFormMode.CreatingFolder
            )
        )
}