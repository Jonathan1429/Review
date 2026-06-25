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
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.mapper.toInt

@Composable
fun BoxItemFolder(
    folderColor: ColorType,
    iconRes: IconType,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val backgroundColorFolder = folderColor.toInt(isDark)
    val selectedIcon = iconRes.toInt()
    val backgroundColor = Color(backgroundColorFolder).copy(alpha = 50f / 255f)

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor), // Fondo con transparencia
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = selectedIcon),
            contentDescription = "Preview Folder Icon",
            modifier = Modifier.size(75.dp),
            // 2. El icono lleva el color sólido seleccionado
            tint = Color(backgroundColorFolder)
        )
    }
}