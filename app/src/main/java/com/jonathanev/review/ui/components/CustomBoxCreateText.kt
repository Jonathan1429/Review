package com.jonathanev.review.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R

@OptIn(ExperimentalFoundationApi::class)
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val type = event.type
                    }
                }
            }
            .verticalScroll(scrollState)
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
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !readOnly
                        ) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                ) {
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