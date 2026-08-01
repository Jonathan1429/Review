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
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.mapper.toInt
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.BoxItemFolderDataProvider
import com.jonathanev.review.ui.preview.providers.PropertiesItemFolder
import com.jonathanev.review.ui.theme.ReviewTheme

@ComponentsPreviews
@Composable
fun PreviewBoxItemFolder(
    @PreviewParameter(BoxItemFolderDataProvider::class) data: PropertiesItemFolder
) {
    ReviewTheme {
        BoxItemFolder(data.iconRes, data.iconcolor)
    }
}

@Composable
fun BoxItemFolder(
    iconRes: IconType,
    iconColor: ColorType,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val iconColor = iconColor.toInt(isDark)
    val colorTwentyPercent = Color(iconColor).copy(alpha = 50f / 255f)
    val selectedIcon = iconRes.toInt()

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colorTwentyPercent),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = selectedIcon),
            contentDescription = "Preview Folder Icon",
            modifier = Modifier.size(75.dp),
            tint = Color(iconColor)
        )
    }
}