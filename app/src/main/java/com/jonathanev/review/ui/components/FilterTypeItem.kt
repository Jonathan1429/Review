package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jonathanev.review.ui.mapper.toDrawable
import com.jonathanev.review.ui.model.ContentType
import kotlin.collections.forEach

@Composable
fun FilterTypeItem(
    mediaForSelected: List<ContentType>,
    mediaSelected: ContentType,
    onFilterClicked: (ContentType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        mediaForSelected.forEach { item ->
            FilterChipItem(
                itemContentType = item,
                iconRes = item.toDrawable(),
                contentTypeSelected = mediaSelected,
                onFilterClicked = { filterClicked ->
                    onFilterClicked(filterClicked)
                }
            )
        }
    }
}