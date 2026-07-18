package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FolderAttributesUi
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.presentation.model.IconType

class ListFoldersDataProvider : PreviewParameterProvider<List<FolderUiModel>> {
    override val values: Sequence<List<FolderUiModel>> = sequenceOf(
        listOf(
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
    )
}