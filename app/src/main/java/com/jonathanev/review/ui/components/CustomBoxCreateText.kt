package com.jonathanev.review.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R

@Composable
fun CustomBoxCreateText(
    modifier: Modifier = Modifier,
    textValue: TextFieldValue,
    hint: Boolean,
    readOnly: Boolean = false,
    onTextValueChange: (TextFieldValue) -> Unit,
    selectedColor: Color
) {
    val hintText = if (hint) stringResource(R.string.lblCuestionario) else ""
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var containerHeight by remember { mutableStateOf(0) }

    // Convertimos dps a px para el margen de respiro alrededor del cursor
    val density = LocalDensity.current
    val extraPaddingPx = with(density) { 80.dp.toPx().toInt() }

    suspend fun scrollToCursorIfNeeded() {
        val layout = textLayoutResult ?: return
        if (containerHeight <= 0) return

        val cursorOffset = textValue.selection.start
        val line = layout.getLineForOffset(cursorOffset.coerceIn(0, textValue.text.length))

        // Puntos de referencia vertical de la línea actual
        val lineBottom = layout.getLineBottom(line).toInt()
        val lineTop = layout.getLineTop(line).toInt()

        val currentScroll = scrollState.value
        val visibleBottom = currentScroll + containerHeight

        // Si la línea (o el salto con Enter) toca o rebasa la zona visible inferior
        if (lineBottom + extraPaddingPx > visibleBottom) {
            val targetScroll = (lineBottom + extraPaddingPx) - containerHeight
            scrollState.animateScrollTo(targetScroll.coerceAtMost(scrollState.maxValue))
        }
        // Si el cursor está por encima del borde superior visible
        else if (lineTop - extraPaddingPx < currentScroll) {
            val targetScroll = (lineTop - extraPaddingPx).coerceAtLeast(0)
            scrollState.animateScrollTo(targetScroll)
        }
    }

    // Se ejecuta al cambiar el texto/cursor o cuando el tamaño del contenedor cambia (teclado abre/cierra)
    LaunchedEffect(textValue.selection, containerHeight) {
        if (!scrollState.isScrollInProgress) {
            scrollToCursorIfNeeded()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeight = it.height }
            // 1. Prioridad absoluta al gesto manual de desplazamiento
            .verticalScroll(scrollState)
            // 2. Toque en zona vacía para enfocar sin interceptar scroll
            .pointerInput(readOnly) {
                if (!readOnly) {
                    detectTapGestures {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
            }
            .padding(20.dp)
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = onTextValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            readOnly = readOnly,
            cursorBrush = SolidColor(selectedColor),
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            onTextLayout = { layoutResult ->
                textLayoutResult = layoutResult
            },
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (textValue.text.isEmpty()) {
                        Text(
                            text = hintText,
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}