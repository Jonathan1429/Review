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

    val density = LocalDensity.current
    val extraPaddingPx = with(density) { 80.dp.toPx().toInt() }

    // Función suspendida limpia sin bucles
    suspend fun scrollToCursorIfNeeded() {
        val layout = textLayoutResult ?: return
        if (containerHeight <= 0) return

        val cursorOffset = textValue.selection.start
        val line = layout.getLineForOffset(cursorOffset.coerceIn(0, textValue.text.length))

        val lineBottom = layout.getLineBottom(line).toInt()
        val lineTop = layout.getLineTop(line).toInt()

        val currentScroll = scrollState.value
        val visibleBottom = currentScroll + containerHeight

        if (lineBottom + extraPaddingPx > visibleBottom) {
            val targetScroll = (lineBottom + extraPaddingPx) - containerHeight
            // Usamos scrollTo en lugar de animateScrollTo para evitar animaciones encadenadas en bucle
            scrollState.scrollTo(targetScroll.coerceAtMost(scrollState.maxValue))
        } else if (lineTop - extraPaddingPx < currentScroll) {
            val targetScroll = (lineTop - extraPaddingPx).coerceAtLeast(0)
            scrollState.scrollTo(targetScroll)
        }
    }

    // SOLO se dispara cuando cambia la selección, Y cuando no hay un scroll manual del usuario
    LaunchedEffect(textValue.selection.start, containerHeight) {
        if (!scrollState.isScrollInProgress) {
            scrollToCursorIfNeeded()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerHeight = it.height }
            .verticalScroll(scrollState)
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