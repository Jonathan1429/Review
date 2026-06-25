package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType

data class PropertiesItemFolder(
    val folderColor: ColorType,
    val iconRes: IconType
)

class BoxItemFolderDataProvider : PreviewParameterProvider<PropertiesItemFolder> {
    override val values: Sequence<PropertiesItemFolder> = sequenceOf(
        PropertiesItemFolder(folderColor = ColorType.Black, iconRes = IconType.ANCHOR_SOLID_FULL),
        PropertiesItemFolder(folderColor = ColorType.Gray, iconRes = IconType.BACTERIA_SOLID_FULL),
        PropertiesItemFolder(folderColor = ColorType.RandomColor(Color.Red.toArgb()), iconRes = IconType.ANCHOR_SOLID_FULL)
    )
}