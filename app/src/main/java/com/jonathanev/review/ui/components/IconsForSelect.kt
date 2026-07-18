package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.IconsDataProvider
import com.jonathanev.review.ui.preview.providers.PropertiesFolderSelected
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewIconsForSelected(
    @PreviewParameter(IconsDataProvider::class) data: PropertiesFolderSelected
) {
    ReviewTheme {
        IconsForSelect(data.listIcons, data.posSelected) { _, _ -> }
    }
}

@Composable
fun IconsForSelect(
    icons: List<IconType>,
    positionIcon: Int,
    onChangeIcon: (Int, IconType) -> Unit,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(1),
        modifier = Modifier.height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(icons) { index, icon ->
            LayeredSelectedIcon(
                isSelected = positionIcon == index,
                icon = icon,
            ) { onChangeIcon(index, icon) }
        }
    }
}