package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomTextField(name: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = name,
        onValueChange = { onValueChange(it) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        label = { Text(label) },
    )
}