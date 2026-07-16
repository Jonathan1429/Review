package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.mapper.toInt
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.BoxItemFolderDataProvider
import com.jonathanev.review.ui.preview.providers.PropertiesItemFolder
import com.jonathanev.review.ui.theme.Black
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.White

//@DevicePreviews
@Composable
fun PreviewBoxItemFolder(
    @PreviewParameter(BoxItemFolderDataProvider::class) data: PropertiesItemFolder
) {
    ReviewTheme {
        BoxItemFolder(data.iconRes)
    }
}

@Composable
fun BoxItemFolder(
    iconRes: IconType,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) White else Black
    val colorTwentyPercent = Color(backgroundColor.value).copy(alpha = 50f / 255f)
    val selectedIcon = iconRes.toInt()

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorTwentyPercent), // Fondo con transparencia
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = selectedIcon),
            contentDescription = "Preview Folder Icon",
            modifier = Modifier.size(75.dp),
            tint = backgroundColor
        )
    }
}