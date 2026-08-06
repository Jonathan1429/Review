package com.jonathanev.review.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.jonathanev.review.R

@Composable
fun DialogConfirmDelete(
    description: String,
    onDeleteItemClick: () -> Unit,
    onCloseDialog: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCloseDialog,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_advertencia),
                contentDescription = "Advertencia"
            )
        },
        title = {
            Text(text = "¡Atención!")
        },
        text = { Text(text = description) },
        confirmButton = {
            TextButton(onClick = singleClick { onDeleteItemClick() }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = singleClick { onCloseDialog() }) {
                Text("Cancelar")
            }
        }
    )
}