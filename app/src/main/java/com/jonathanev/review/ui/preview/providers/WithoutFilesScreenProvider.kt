package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.FileInteractionMode

class WithoutFilesScreenProvider : PreviewParameterProvider<FileInteractionMode> {
    override val values: Sequence<FileInteractionMode>
        get() = sequenceOf(
            FileInteractionMode.Default,
            FileInteractionMode.MovingItem
        )

    override fun getDisplayName(index: Int): String? {
        return when (index) {
            0 -> "Default"
            1 -> "MovingItem"
            else -> super.getDisplayName(index)
        }
    }
}