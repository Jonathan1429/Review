package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jonathanev.review.R

@Composable
fun <T> DialogOptionsMenu(
    options: List<T>,
    optionTitle: (T) -> String,
    onOptionSelected: (T) -> Unit,
    onCloseDialog: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onCloseDialog()
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_advertencia),
                contentDescription = "Advertencia"
            )
        },
        title = {
            Text(text = "¿Qué acción deseas realizar?")
        },
        text = {
            LazyColumn {
                items(options) { option ->
                    Text(
                        text = optionTitle(option),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .singleClick(onClick = { onOptionSelected(option) })
                            .padding(vertical = 14.dp, horizontal = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = singleClick { onCloseDialog() }) {
                Text("Cancelar")
            }
        }
    )
}