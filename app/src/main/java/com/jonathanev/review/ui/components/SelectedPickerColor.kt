package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.baseColor

@ComponentsPreviews
@Composable
fun PreviewSelectedPickerColor() {
    ReviewTheme {
        SelectedPickerColor { }
    }
}

@Composable
fun SelectedPickerColor(onChangeColor: (Int) -> Unit) {
    val controller = rememberColorPickerController()
    val color = baseColor

    LaunchedEffect(Unit) {
        controller.selectByColor(color = color, fromUser = false)
    }

    HsvColorPicker(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(10.dp),
        controller = controller,
        onColorChanged = { colorEnvelope: ColorEnvelope ->
            onChangeColor(colorEnvelope.color.toArgb())
        },
    )
}