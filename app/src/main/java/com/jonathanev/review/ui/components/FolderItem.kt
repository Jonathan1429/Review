package com.jonathanev.review.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
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
        FolderItem(data.listFolders[0]) { }
    }
}

@Composable
fun FolderItem(
    guia: FolderUiModel,
    onClick: () -> Unit
) {
    //val color50 = ColorUtils.setAlphaComponent(guia.folder.color.toColorRes(), 50)
    val isDark = isSystemInDarkTheme()
    val backgroundColor = Color(guia.folder.color.toInt(isDark))
    val colorTwentyPercent = backgroundColor.copy(alpha = 0.2f)

    Card(
        colors = CardDefaults.cardColors(containerColor = getCardContainerColor()),
        modifier = Modifier
            .fillMaxWidth()
            .singleClick(onClick = { onClick() })
            .padding(4.dp)
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${guia.numGuides} Guias",
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}