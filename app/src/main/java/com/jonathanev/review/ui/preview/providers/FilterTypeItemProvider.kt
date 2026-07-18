package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.ui.model.ContentType

data class FilterTypeItemProv(
    val media: List<ContentType>,
    val mediaSelected: ContentType
) {
    override fun toString(): String {
        return "Selected_$mediaSelected"
    }
}

class FilterTypeItemProvider: PreviewParameterProvider<FilterTypeItemProv>{
    override val values: Sequence<FilterTypeItemProv>
        get() = sequenceOf(
            FilterTypeItemProv(
                media = listOf(ContentType.TEXT, ContentType.IMAGE),
                mediaSelected = ContentType.TEXT
            ),
            FilterTypeItemProv(
                media = listOf(ContentType.TEXT, ContentType.IMAGE),
                mediaSelected = ContentType.IMAGE
            )
        )

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Selected_Text"
            1 -> "Selected_Image"
            else -> super.getDisplayName(index)
        }
    }
}