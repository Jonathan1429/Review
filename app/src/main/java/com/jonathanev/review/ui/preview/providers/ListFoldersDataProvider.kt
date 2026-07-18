package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.FolderAttributesUi
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.model.IconType

data class ListFoldersDataProv(
    val listFolders: List<FolderUiModel>,
    val fildeInteractionMode: FileInteractionMode
)

val list = listOf(
    FolderUiModel(
        folder = FolderAttributesUi(
            name = "Abap",
            imgFolder = IconType.BACTERIA_SOLID_FULL,
            color = ColorType.White
        ),
        numGuides = 5
    ),
    FolderUiModel(
        folder = FolderAttributesUi(
            name = "Kotlin",
            imgFolder = IconType.ANCHOR_SOLID_FULL,
            color = ColorType.White
        ),
        numGuides = 6
    ),
    FolderUiModel(
        folder = FolderAttributesUi(
            name = "SQL",
            imgFolder = IconType.BACTERIA_SOLID_FULL,
            color = ColorType.White
        ),
        numGuides = 5
    ),
    FolderUiModel(
        folder = FolderAttributesUi(
            name = "Ingles",
            imgFolder = IconType.ANCHOR_SOLID_FULL,
            color = ColorType.White
        ),
        numGuides = 6
    )
)

class ListFoldersDataProvider : PreviewParameterProvider<ListFoldersDataProv> {
    override val values: Sequence<ListFoldersDataProv> = sequenceOf(
        ListFoldersDataProv(
            listFolders = list,
            fildeInteractionMode = FileInteractionMode.MovingItem
        ),
        ListFoldersDataProv(
            listFolders = list,
            fildeInteractionMode = FileInteractionMode.Default
        )
    )
}