package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.theme.ReviewTheme

@ComponentsPreviews
@Composable
fun PreviewColorPickerDialog() {
    ReviewTheme {
        val controller = rememberColorPickerController()

        ColorPickerDialogContent(
            controller = controller,
            colorInitial = MaterialTheme.colorScheme.onSurface,
            selectedColor = MaterialTheme.colorScheme.onSurface,
            onDismissRequest = {},
            onColorSelected = {},
            onDefaultClick = {}
        )
    }
}

@Composable
fun ColorPickerDialog(
    colorInitial: Color,
    selectedColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onDefaultClick: () -> Unit
) {
    val controller = rememberColorPickerController()

    Dialog(onDismissRequest = onDismissRequest) {
        ColorPickerDialogContent(
            controller,
            colorInitial,
            onColorSelected,
            selectedColor,
            onDismissRequest,
            onDefaultClick
        )
    }
}

@Composable
fun ColorPickerDialogContent(
    controller: ColorPickerController,
    colorInitial: Color,
    onColorSelected: (Color) -> Unit,
    selectedColor: Color,
    onDismissRequest: () -> Unit,
    onDefaultClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(320.dp, 350.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HsvColorPicker(
                modifier = Modifier.size(200.dp),
                controller = controller,
                initialColor = colorInitial,
                onColorChanged = { colorEnvelope ->
                    onColorSelected(colorEnvelope.color)
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        onColorSelected(selectedColor)
                        onDismissRequest()
                    }
                ) {
                    Text("Continuar")
                }

                OutlinedButton(
                    onClick = {
                        onDefaultClick()
                        onDismissRequest()
                    }
                ) {
                    Text(
                        text = "Default",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}