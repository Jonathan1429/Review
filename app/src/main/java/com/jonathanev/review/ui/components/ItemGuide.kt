package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.GuideUiModel
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.ItemGuideProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getCardContainerColor
import com.jonathanev.review.ui.theme.getColorSubtitle

@ComponentsPreviews
@Composable
fun PreviewItemGuid3(
    @PreviewParameter(ItemGuideProvider::class) data: GuideUiModel
) {
    ReviewTheme {
        ItemGuide(data) { }
    }
}

@Composable
fun ItemGuide(
    guide: GuideUiModel,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = getCardContainerColor()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .singleClick(onClick = { onClick() })
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ShapeDefaults.ExtraLarge)
                .singleClick(onClick = { onClick() })
                .padding(vertical = 14.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lightbulb_solid_full),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = guide.nameGuide,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = guide.description,
                    color = getColorSubtitle(),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}