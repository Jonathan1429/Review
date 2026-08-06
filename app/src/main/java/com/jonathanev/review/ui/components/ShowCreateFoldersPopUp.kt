package com.jonathanev.review.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ShowCreateFoldersPopUp(
    modifier: Modifier = Modifier,
    onRetryRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { },
        title = {
            Text(text = "Error")
        },
        text = {
            Text(text = "No se pudieron crear los ficheros correctamente")
        },
        confirmButton = {
            TextButton(
                onClick = singleClick { onRetryRequest() }
            ) {
                Text(text = "Reintentar", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        dismissButton = {
            TextButton(
                onClick = singleClick {
                    onDismissRequest()
                }
            ) {
                Text(text = "Cancelar", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}