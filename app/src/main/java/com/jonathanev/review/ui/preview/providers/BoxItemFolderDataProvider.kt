package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType

data class PropertiesItemFolder(
    val iconRes: IconType
)

class BoxItemFolderDataProvider : PreviewParameterProvider<PropertiesItemFolder> {
    override val values: Sequence<PropertiesItemFolder> = sequenceOf(
        PropertiesItemFolder(iconRes = IconType.ANCHOR_SOLID_FULL),
        PropertiesItemFolder(iconRes = IconType.BACTERIA_SOLID_FULL),
        PropertiesItemFolder(iconRes = IconType.ANCHOR_SOLID_FULL)
    )
}