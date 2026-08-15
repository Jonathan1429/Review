package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.FileFormMode
import com.jonathanev.review.presentation.model.IconType

data class CardBoxItemFolderProv(
    val icon: IconType,
    val color: ColorType,
    val fileFormMode: FileFormMode
)

class CardBoxItemFolderProviders : PreviewParameterProvider<CardBoxItemFolderProv> {
    override val values: Sequence<CardBoxItemFolderProv>
        get() = sequenceOf(
            CardBoxItemFolderProv(
                icon = IconType.BACTERIA_SOLID_FULL,
                color = ColorType.Default,
                fileFormMode = FileFormMode.CreatingFile
            ),
            CardBoxItemFolderProv(
                icon = IconType.BACTERIA_SOLID_FULL,
                color = ColorType.Default,
                fileFormMode = FileFormMode.CreatingFolder
            )
        )
}