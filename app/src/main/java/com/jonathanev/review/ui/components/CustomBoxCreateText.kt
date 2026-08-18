package com.jonathanev.review.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
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
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val isImeVisible = WindowInsets.isImeVisible
    val density = LocalDensity.current
    val extraBottomMarginPx = remember(density) { with(density) { 16.dp.toPx() } }

    suspend fun bringCursorIntoView() {
        textLayoutResult?.let { layoutResult ->
            val cursorOffset = textValue.selection.start
            if (cursorOffset in 0..layoutResult.layoutInput.text.length) {
                val cursorRect = layoutResult.getCursorRect(cursorOffset)
                val inflatedRect = Rect(
                    left = cursorRect.left,
                    top = cursorRect.top,
                    right = cursorRect.right,
                    bottom = cursorRect.bottom + extraBottomMarginPx
                )
                bringIntoViewRequester.bringIntoView(inflatedRect)
            }
        }
    }

    LaunchedEffect(textValue.selection, textLayoutResult, isImeVisible) {
        if (isImeVisible) {
            delay(100.milliseconds)
        }
        bringCursorIntoView()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = onTextValueChange,
            modifier = Modifier
                .fillMaxSize()
                .bringIntoViewRequester(bringIntoViewRequester),
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
                Box(modifier = Modifier.fillMaxSize()) {
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