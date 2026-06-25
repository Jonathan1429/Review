package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.IconType
import com.jonathanev.review.ui.theme.iconBackground

@Composable
fun LayeredSelectedIcon(
    icon: IconType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Definimos los colores (puedes usar tus colores de Theme.kt)
    val itemIconColor = Color(0xFF6370E7) // Equivalente a @color/item_icon
    val backgroundColor = Color(0xFFF5F5F5) // Equivalente a @color/bg_edittext
    val iconDrawable = when (icon) {
        IconType.LIGHTBULB -> R.drawable.ic_lightbulb_solid_full
        IconType.ANCHOR_SOLID_FULL -> R.drawable.ic_anchor_solid_full
        IconType.ANGELLIST_BRANDS_SOLID_FULL -> R.drawable.ic_angellist_brands_solid_full
        IconType.BACTERIA_SOLID_FULL -> R.drawable.ic_bacteria_solid_full
    }

    Box(
        modifier = modifier
            .size(56.dp) // Tamaño total del contenedor
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier
                        .background(itemIconColor) // Capa exterior (Capa 1)
                        .padding(2.dp)
                        .background(
                            backgroundColor,
                            RoundedCornerShape(7.dp)
                        ) // Capa media (Capa 2)
                        .padding(2.dp)
                        .background(
                            itemIconColor,
                            RoundedCornerShape(6.dp)
                        ) // Capa interior (Capa 3)
                } else {
                    Modifier.background(iconBackground) // Estado normal
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconDrawable),
            contentDescription = null,
            tint = if (isSelected) Color.White else itemIconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}