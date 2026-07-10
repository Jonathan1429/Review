package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

@Composable
fun OptionsCreateImage(uriImage: String, selectImage: () -> Unit, imageUploaded: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = selectImage,
            modifier = Modifier
                .padding(end = 10.dp)
                .size(34.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_file_image),
                contentDescription = "Limpiar Color",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uriImage.isNotEmpty()) {
            IconButton(
                onClick = { imageUploaded },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_success),
                    contentDescription = "Confirmar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}