package com.jonathanev.review.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.baseColor
import com.jonathanev.review.ui.theme.getCardContainerColor

@ComponentsPreviews
@Composable
fun PreviewSelectedPickerColor() {
    ReviewTheme {
        SelectedPickerColor { }
    }
}

/*@Composable
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
}*/

@Composable
fun SelectedPickerColor(
    onChangeColor: (Int) -> Unit
) {
    val controller = rememberColorPickerController()
    val color = baseColor

    LaunchedEffect(color) {
        controller.selectByColor(color = color, fromUser = false)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = getCardContainerColor()
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        if (colorEnvelope.fromUser) {
                            onChangeColor(colorEnvelope.color.toArgb())
                        }
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(controller.selectedColor.value)
                        .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )

                Column {
                    Text(
                        text = "Color Seleccionado",
                        fontSize = 12.sp,
                        color = Color(0xFFC4C5D9)
                    )
                    Text(
                        text = "#${
                            controller.selectedColor.value.toArgb().toUInt().toString(16)
                                .takeLast(6).uppercase()
                        }",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB8C3FF)
                    )
                }
            }
        }
    }
}