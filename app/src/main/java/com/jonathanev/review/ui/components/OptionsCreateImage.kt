package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jonathanev.review.R
import com.jonathanev.review.presentation.model.GuideMode

@Composable
fun OptionsCreateImage(
    uriImage: String,
    imageUploaded: () -> Unit,
    guideMode: GuideMode,
    onBackNav: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = {
                if (guideMode is GuideMode.Review) {
                    onBackNav()
                } else {
                    if (uriImage.isNotEmpty()) {
                        imageUploaded()
                    } else {
                        onBackNav()
                    }
                }
            },
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_success),
                contentDescription = "Confirmar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}