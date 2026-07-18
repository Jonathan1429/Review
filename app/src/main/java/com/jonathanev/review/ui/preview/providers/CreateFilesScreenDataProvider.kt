package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.presentation.state.PropertiesFilesState
import com.jonathanev.review.presentation.state.PropertiesFilesState.Companion.FileIconsMock
import com.jonathanev.review.presentation.state.PropertiesFilesState.Companion.FolderIconsMock

data class PropertiesCreateFilesScreen(
    val state: PropertiesFilesState,
    val fileFormMode: FileFormMode
)

class CreateFilesScreenDataProvider : PreviewParameterProvider<PropertiesCreateFilesScreen> {
    override val values: Sequence<PropertiesCreateFilesScreen>
        get() = sequenceOf(
            PropertiesCreateFilesScreen(
                state = PropertiesFilesState(
                    name = "",
                    description = "",
                    icon = FileIconsMock[0],
                    color = ColorType.White,
                    selectedIndex = 0,
                    icons = FileIconsMock,
                    showOverwriteDialogFile = false
                ),
                fileFormMode = FileFormMode.CreatingFile
            ),
            PropertiesCreateFilesScreen(
                state = PropertiesFilesState(
                    name = "Testing",
                    description = "",
                    icon = FileIconsMock[0],
                    color = ColorType.White,
                    selectedIndex = 0,
                    icons = FileIconsMock,
                    showOverwriteDialogFile = true
                ),
                fileFormMode = FileFormMode.RenameFile(
                    GuideUiModel(
                        nameGuide = "Test",
                        description = "Testing Unitarios"
                    )
                )
            ),
            PropertiesCreateFilesScreen(
                state = PropertiesFilesState(
                    name = "",
                    description = "",
                    icon = FolderIconsMock[1],
                    color = ColorType.White,
                    selectedIndex = 1,
                    icons = FolderIconsMock,
                    showOverwriteDialogFile = false
                ),
                fileFormMode = FileFormMode.CreatingFolder
            )
        )
}