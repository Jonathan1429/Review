package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.SpanPalabraModel
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.ColorPickerDialog
import com.jonathanev.review.ui.components.CustomBoxCreateText
import com.jonathanev.review.ui.components.OptionsCreateText
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.degradientColor

@Preview(showBackground = true)
@Composable
fun PreviewCreateTextScreen() {
    ReviewTheme {
        CreateTextScreen(
            guideMode = GuideMode.Review("", 0),
            contentType = QuestionContentUi.Text("", emptyList()),
            onSaveText = { _, _ -> }
        )
    }
}

@Composable
fun CreateTextRoute(
    guideMode: GuideMode,
    viewModel: SharedFragmentCreateFileViewModel,
    contentType: QuestionContentUi.Text,
    onSaveText: () -> Unit
) {
    CreateTextScreen(
        guideMode = guideMode,
        contentType = contentType,
        onSaveText = { text, colors ->
            viewModel.addTextContent(textWithLabels = text, listSpans = colors)
            onSaveText()
        }
    )
}

@Composable
fun CreateTextScreen(
    modifier: Modifier = Modifier,
    guideMode: GuideMode,
    contentType: QuestionContentUi.Text,
    onSaveText: (String, List<ColorRangeUi>) -> Unit,
) {
    val colorInitial = MaterialTheme.colorScheme.onSurface
    var selectedColor by remember { mutableStateOf(colorInitial) }
    var textValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(annotatedString = contentType.toAnnotatedString()))
    }
    var showDialog by remember { mutableStateOf(false) }

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
                if (guideMode !is GuideMode.Review) {
                    OptionsCreateText(
                        textValue = textValue.annotatedString,
                        selectedColor = selectedColor,
                        onClearColorClick = {
                            textValue = TextFieldValue(text = textValue.text)
                        },
                        onSelectColorClick = { showDialog = true },
                        onSaveTextClick = {
                            saveCurrentQuestion(
                                textFieldValue = textValue,
                                onSaveContent = { text, colors -> onSaveText(text, colors) }
                            )
                        }
                    )
                }
                CustomBoxCreateText(
                    textValue = textValue,
                    hint = textValue.text.isNotEmpty(),
                    onTextValueChange = { actualText ->
                        val oldText = textValue.text
                        val newText = actualText.text
                        val isSingleCharacterAdded = (newText.length - oldText.length) == 1
                        val cursorPosition = actualText.selection.start
                        val addedChar =
                            if (cursorPosition > 0 && cursorPosition <= newText.length) {
                                newText[cursorPosition - 1]
                            } else {
                                null
                            }

                        if (isSingleCharacterAdded && addedChar != '\n' && selectedColor != colorInitial) {
                            val newAnnotatedString = applyColorToCharacter(
                                currentAnnotatedString = actualText.annotatedString,
                                cursorPosition = cursorPosition,
                                color = selectedColor
                            )

                            textValue = actualText.copy(annotatedString = newAnnotatedString)
                        } else {
                            textValue = actualText
                        }
                    }
                )
            }

            if (showDialog) {
                ColorPickerDialog(
                    colorInitial = colorInitial,
                    selectedColor = selectedColor,
                    onDismissRequest = { showDialog = false },
                    onColorSelected = { colorActual ->
                        selectedColor = colorActual
                    },
                    onDefaultClick = { selectedColor = colorInitial }
                )
            }
        }
    }
}

private fun applyColorToCharacter(
    currentAnnotatedString: AnnotatedString,
    cursorPosition: Int,
    color: Color
): AnnotatedString {
    if (cursorPosition <= 0) return currentAnnotatedString

    val targetIndex = cursorPosition - 1
    return buildAnnotatedString {
        append(currentAnnotatedString)

        addStyle(
            style = SpanStyle(color = color),
            start = targetIndex,
            end = cursorPosition
        )
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

private fun saveCurrentQuestion(
    textFieldValue: TextFieldValue,
    onSaveContent: (String, List<ColorRangeUi>) -> Unit
): SpanPalabraModel {
    val annotatedString = textFieldValue.annotatedString
    val text = annotatedString.text
    val spanStyles = annotatedString.spanStyles
    var isDoubleColors = false
    val listSpans = mutableListOf<ColorRangeUi>()

    spanStyles.forEachIndexed { index, range ->
        if (range.item.color != Color.Unspecified) {
            if (index > 0) {
                val previousRange = spanStyles[index - 1]
                if (range.start < previousRange.end) {
                    isDoubleColors = true
                }
            }

            listSpans.add(
                ColorRangeUi(
                    start = range.start,
                    end = range.end,
                    color = range.item.color.toArgb()
                )
            )
        }
    }

    onSaveContent(text, listSpans)

    return if (isDoubleColors) {
        SpanPalabraModel(
            message = "Sobreescribiste colores y mantuvimos los últimos seleccionados",
            isDoubleColors = true
        )
    } else {
        SpanPalabraModel()
    }
}