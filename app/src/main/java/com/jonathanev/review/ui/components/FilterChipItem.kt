package com.jonathanev.review.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.FilterChipItemProv
import com.jonathanev.review.ui.preview.providers.FilterChipItemProvider
import com.jonathanev.review.ui.theme.ComponentTheme
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.TextColorSecondary

@ComponentsPreviews
@Composable
fun PreviewFilterChipItem(
    @PreviewParameter(FilterChipItemProvider::class) data: FilterChipItemProv
) {
    ReviewTheme {
        FilterChipItem(
            itemContentType = data.itemContentType,
            iconRes = data.iconRes,
            contentTypeSelected = data.contentTypeSelected,
            onFilterTypeClicked = {}
        )
    }
}

@Composable
fun FilterChipItem(
    itemContentType: ContentType,
    iconRes: Int,
    contentTypeSelected: ContentType,
    onFilterTypeClicked: (ContentType) -> Unit
) {
    Box(
        modifier = Modifier
            .singleClick(onClick = { onFilterTypeClicked(itemContentType) })
            .height(40.dp)
            .then(
                if (itemContentType == contentTypeSelected) {
                    Modifier
                        .border(
                            border = BorderStroke(2.dp, ComponentTheme.getSelectedBorderBrush()),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            brush = ComponentTheme.getSelectedBackgroundBrush(),
                            shape = RoundedCornerShape(8.dp)
                        )
                } else {
                    Modifier
                        .border(
                            border = BorderStroke(1.dp, ComponentTheme.getUnselectedBorderColor()),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = ComponentTheme.getUnselectedBackgroundColor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                }
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = TextColorSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (itemContentType == ContentType.TEXT)
                    stringResource(R.string.lblText)
                else
                    stringResource(R.string.lblImage),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}