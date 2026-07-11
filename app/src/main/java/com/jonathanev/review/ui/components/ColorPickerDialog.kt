package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

@Composable
fun ColorPickerDialog(
    colorInitial: Color,
    selectedColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit,
    onDefaultClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.size(350.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    AndroidView(
                        factory = { context ->
                            ColorPickerView(context).apply {
                                setInitialColor(colorInitial.toArgb())
                                setColorListener(ColorEnvelopeListener { envelope, _ ->
                                    val parsedColor = Color(android.graphics.Color.parseColor(envelope.hexCode))
                                    onColorSelected(parsedColor)
                                })
                            }
                        },
                        modifier = Modifier.size(160.dp)
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

                        Button(
                            onClick = {
                                onDefaultClick()
                                onDismissRequest()
                            }
                        ) {
                            Text("Default")
                        }
                    }
                }
            }
        }
    }
}