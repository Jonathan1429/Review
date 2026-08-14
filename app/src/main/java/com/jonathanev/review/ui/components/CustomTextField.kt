package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.CustomTextFieldDataProvider
import com.jonathanev.review.ui.preview.providers.PropertiesTF
import com.jonathanev.review.ui.theme.ReviewTheme
import java.text.Normalizer

@ComponentsPreviews
@Composable
fun PreviewCustomTextField(
    @PreviewParameter(CustomTextFieldDataProvider::class) data: PropertiesTF
) {
    ReviewTheme {
        CustomTextField(data.name, data.label) { }
    }
}

private val allowedCharsRegex = Regex("[^a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ_-]")

@Composable
fun CustomTextField(name: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = name,
        onValueChange = { rawInput ->
            val normalized = Normalizer.normalize(rawInput, Normalizer.Form.NFC)

            val sanitized = normalized
                .replace(allowedCharsRegex, "")
                .replace(Regex("\\s+"), " ")

            onValueChange(sanitized)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        label = { Text(text = label, color = MaterialTheme.colorScheme.onSurface) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            autoCorrectEnabled = true
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),

            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),

            cursorColor = MaterialTheme.colorScheme.onSurface,

            selectionColors = TextSelectionColors(
                handleColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    )
}