package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.R
import com.jonathanev.review.ui.model.ContentType

data class FilterChipItemProv(
    val itemContentType: ContentType,
    val iconRes: Int,
    val contentTypeSelected: ContentType
) {
    override fun toString(): String {
        return "Item: ${itemContentType.name} - Selected: ${contentTypeSelected.name}"
    }
}

class FilterChipItemProvider: PreviewParameterProvider<FilterChipItemProv> {
    override val values: Sequence<FilterChipItemProv>
        get() = sequenceOf(
            FilterChipItemProv(
                itemContentType = ContentType.TEXT,
                iconRes = R.drawable.ic_file,
                contentTypeSelected = ContentType.TEXT
            ),
            FilterChipItemProv(
                itemContentType = ContentType.TEXT,
                iconRes = R.drawable.ic_file,
                contentTypeSelected = ContentType.IMAGE
            ),
            FilterChipItemProv(
                itemContentType = ContentType.IMAGE,
                iconRes = R.drawable.ic_image,
                contentTypeSelected = ContentType.IMAGE
            ),
            FilterChipItemProv(
                itemContentType = ContentType.IMAGE,
                iconRes = R.drawable.ic_image,
                contentTypeSelected = ContentType.TEXT
            ),
        )

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Text - Selected: Text"
            1 -> "Text - Selected: Image"
            2 -> "Image - Selected: Image"
            3 -> "Image - Selected: Text"
            else -> super.getDisplayName(index)
        }
    }
}