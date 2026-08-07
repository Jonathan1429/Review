package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R

@Composable
fun CustomBoxCreateText(
    modifier: Modifier = Modifier,
    textValue: TextFieldValue,
    hint: Boolean,
    enabled: Boolean = false,
    onTextValueChange: (TextFieldValue) -> Unit
) {
    val hint = if (hint) stringResource(R.string.lblCuestionario) else ""

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = onTextValueChange,
            modifier = Modifier.fillMaxSize(),
            enabled = enabled,
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize()) {
                    if (textValue.text.isEmpty()) {
                        Text(
                            text = hint,
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