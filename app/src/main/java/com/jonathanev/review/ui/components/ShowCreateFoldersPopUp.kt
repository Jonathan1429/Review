package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.getAlertDialogColor

@ComponentsPreviews
@Composable
fun PreviewShowCreateFoldersPopUp() {
    ReviewTheme {
        CreateFoldersPopUpContent(
            onRetryRequest = {},
            onDismissRequest = {}
        )
    }
}

@Composable
fun CreateFoldersPopUpContent(
    modifier: Modifier = Modifier,
    onRetryRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = getAlertDialogColor()
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No se pudieron crear los ficheros correctamente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.align(Alignment.End)
            ) {
                TextButton(
                    onClick = singleClick { onDismissRequest() }
                ) {
                    Text(text = "Cancelar", color = MaterialTheme.colorScheme.onSurface)
                }

                TextButton(
                    onClick = singleClick { onRetryRequest() }
                ) {
                    Text(text = "Reintentar", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowCreateFoldersPopUp(
    onRetryRequest: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { }
    ) {
        CreateFoldersPopUpContent(
            onRetryRequest = onRetryRequest,
            onDismissRequest = onDismissRequest
        )
    }
}