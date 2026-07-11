package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.components.CustomBoxCreateText
import com.jonathanev.review.ui.components.OptionsCreateText
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.degradientColor

@Preview(showBackground = true)
@Composable
fun PreviewCreateTextScreen() {
    ReviewTheme {
        CreateTextScreen(contentType = QuestionContentUi.Text("", emptyList()))
    }
}

@Composable
fun CreateTextRoute(contentType: QuestionContentUi.Text) {
    CreateTextScreen(contentType = contentType)
}

@Composable
fun CreateTextScreen(
    modifier: Modifier = Modifier,
    contentType: QuestionContentUi.Text,
    onSelectColorClick: () -> Unit = {}
) {
    var textValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(annotatedString = contentType.toAnnotatedString()))
    }

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(42.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = degradientColor
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                OptionsCreateText(textValue.annotatedString, onClearColorClick = {
                    textValue = TextFieldValue(text = textValue.text)
                })
                CustomBoxCreateText(
                    textValue = textValue,
                    hint = true,
                    onTextValueChange = { actualText -> textValue = actualText }
                )
            }
        }
    }
}

private fun QuestionContentUi.Text.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append(this@toAnnotatedString.text)

        for (colorRange in this@toAnnotatedString.colorRanges) {
            addStyle(
                style = SpanStyle(color = Color(colorRange.color)),
                start = colorRange.start,
                end = colorRange.end
            )
        }
    }
}