package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jonathanev.review.R
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.theme.ColorBotones

@DevicePreviews
@Composable
fun PreviewWarningIconCircle() {
    WarningIconCircle()
}

@Composable
fun WarningIconCircle() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .background(color = ColorBotones, shape = CircleShape)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_advertencia),
            contentDescription = stringResource(R.string.iconAdvertencia),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}