package com.jonathanev.review.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.presentation.model.FolderUiModel
import com.jonathanev.review.ui.mapper.toDrawableRes
import com.jonathanev.review.ui.mapper.toInt
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.ListFoldersDataProv
import com.jonathanev.review.ui.preview.providers.ListFoldersDataProvider
import com.jonathanev.review.ui.theme.Inter
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getCardContainerColor

@ComponentsPreviews
@Composable
fun PreviewFolderItem(
    @PreviewParameter(ListFoldersDataProvider::class) data: ListFoldersDataProv
) {
    ReviewTheme {
        Column {
            FolderItem(data.listFolders[0], isHighlighted = true) { }
            HorizontalDivider(Modifier.size(16.dp), color = Color.Transparent)
            FolderItem(data.listFolders[0], isHighlighted = false) { }
        }
    }
}

@Composable
fun FolderItem(
    guia: FolderUiModel,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = Color(guia.folder.color.toInt(isDark))
    val colorTwentyPercent = backgroundColor.copy(alpha = 0.2f)

    val infiniteTransition = rememberInfiniteTransition(label = "highlight")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isHighlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = alpha)
        } else {
            getCardContainerColor()
        },
        label = "color"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isHighlighted) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        modifier = Modifier
            .fillMaxWidth()
            .singleClick(onClick = { onClick() })
            .padding(4.dp)
            .then(
                if (isHighlighted) {
                    Modifier.graphicsLayer {
                        scaleX = 1.02f
                        scaleY = 1.02f
                    }
                } else Modifier
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RectangleShape)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colorTwentyPercent)
                    .padding(12.dp),
                painter = painterResource(guia.folder.imgFolder.toDrawableRes()),
                colorFilter = ColorFilter.tint(backgroundColor),
                contentDescription = "añadir carpeta"
            )
            HorizontalDivider(Modifier.size(8.dp), color = Color.Transparent)
            Text(
                text = guia.folder.name,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Inter,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${guia.numGuides} Guias",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
