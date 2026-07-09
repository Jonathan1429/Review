package com.jonathanev.review.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
fun OptionsCreateText(textValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { },
            modifier = Modifier
                .padding(end = 10.dp)
                .size(34.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_eraser),
                contentDescription = "Limpiar Color",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier
                .padding(end = 10.dp)
                .size(34.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_palette),
                contentDescription = "Seleccionar Color",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.weight(1f))

        if (textValue.isNotEmpty()) {
            IconButton(
                onClick = {},
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