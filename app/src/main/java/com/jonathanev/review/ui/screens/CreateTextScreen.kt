package com.jonathanev.review.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.SpanPalabraModel
import com.jonathanev.review.presentation.state.GuideScreenUiState
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.ColorPickerDialog
import com.jonathanev.review.ui.components.CustomBoxCreateText
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.OptionsCreateText
import com.jonathanev.review.ui.mapper.toInt
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CreateTextScreenProv
import com.jonathanev.review.ui.preview.providers.CreateTextScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.degradientColor

@DevicePreviews
@Composable
fun PreviewTextScreen(
    @PreviewParameter(CreateTextScreenProvider::class) data: CreateTextScreenProv
) {
    ReviewTheme {
        CreateTextScreen(
            guideContext = data.guideContext,
            colorInitial = MaterialTheme.colorScheme.onSurface,
            selectedColor = MaterialTheme.colorScheme.onSurface,
            textValue = data.textValue,
            showDialog = data.showDialog,
            onClearColorClick = {},
            onShowColorDialog = {},
            onChangeTextValue = {},
            onDissmissDialog = {},
            onColorSelected = {},
            onDefaultColor = {},
            onSaveText = { _, _ -> },
            onBackNav = {}
        )
    }
}

@Composable
fun CreateTextRoute(
    viewModel: SharedFragmentCreateFileViewModel,
    posItem: Int,
    onSaveText: () -> Unit,
    onBackNav: () -> Unit,
    questionContentMode: QuestionContentMode
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is GuideScreenUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is GuideScreenUiState.Error -> {
            ErrorComponent(
                onRetry = viewModel::retryLoad,
                onBack = onBackNav
            )
        }

        is GuideScreenUiState.Success -> {
            val textList by viewModel.textList.collectAsStateWithLifecycle()
            val isDark = isSystemInDarkTheme()
            val item = remember(textList, posItem, questionContentMode) {
                if (questionContentMode == QuestionContentMode.CREATING) {
                    QuestionContentUi.Text("", emptyList())
                } else {
                    textList.getOrNull(posItem) ?: QuestionContentUi.Text("", emptyList())
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    viewModel.clearTextDraft()
                }
            }

            // 2. Inicializamos el borrador con el ítem correcto
            LaunchedEffect(item, questionContentMode) {
                viewModel.initTextDraft(item)
            }


            val colorInitial = MaterialTheme.colorScheme.onSurface
            val colorSelected = state.colorType.toInt(isDark)
            var selectedColorInt by remember(colorSelected) {
                mutableIntStateOf(colorSelected)
            }
            val selectedColor = Color(selectedColorInt)

            val textValueState by viewModel.draftTextValue.collectAsStateWithLifecycle()

            // 3. Si textValueState es null (porque DisposableEffect o clearTextDraft lo limpiaron), usa el item
            val textValue = textValueState ?: remember(item) {
                TextFieldValue(annotatedString = item.toAnnotatedString())
            }

            LaunchedEffect(Unit) {
                viewModel.updateItemTriger.collect {
                    onSaveText()
                }
            }

            CreateTextScreen(
                guideContext = state.guideContext,
                colorInitial = colorInitial,
                selectedColor = selectedColor,
                textValue = textValue,
                showDialog = state.showDialogColor,
                onSaveText = { text, colors ->
                    viewModel.addTextContent(
                        textWithLabels = text,
                        listSpans = colors,
                        questionContentMode
                    )
                },
                onClearColorClick = {
                    viewModel.clearTextDraft()
                },
                onShowColorDialog = viewModel::showDialogSelectColor,
                onChangeTextValue = { textFieldValue ->
                    viewModel.onDraftTextChange(newValue = textFieldValue)
                },
                onDissmissDialog = viewModel::onDismissDialogSelectColor,
                onColorSelected = { actualColor ->
                    viewModel.onChangeColor(actualColor = actualColor)
                },
                onDefaultColor = viewModel::onDefaultcolor,
                onBackNav = onBackNav
            )
        }
    }
}


@Composable
fun CreateTextScreen(
    guideContext: GuideContext,
    onSaveText: (String, List<ColorRangeUi>) -> Unit,
    colorInitial: Color,
    selectedColor: Color,
    textValue: TextFieldValue,
    showDialog: Boolean,
    onClearColorClick: () -> Unit,
    onShowColorDialog: () -> Unit,
    onChangeTextValue: (actualText: TextFieldValue) -> Unit,
    onDissmissDialog: () -> Unit,
    onColorSelected: (Int) -> Unit,
    onDefaultColor: () -> Unit,
    onBackNav: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                OptionsCreateText(
                    guideContext = guideContext,
                    textValue = textValue.annotatedString,
                    selectedColor = selectedColor,
                    onClearColorClick = onClearColorClick,
                    onShowColorDialog = onShowColorDialog,
                    onSaveTextClick = {
                        saveCurrentQuestion(
                            textFieldValue = textValue,
                            onSaveContent = { text, colors -> onSaveText(text, colors) }
                        )
                    },
                    onBackNav = onBackNav
                )
                CustomBoxCreateText(
                    modifier = Modifier.padding(20.dp),
                    textValue = textValue,
                    hint = textValue.text.isNotEmpty(),
                    enabled = guideContext !is GuideContext.Browsing,
                    onTextValueChange = { actualText ->
                        val newAnnotatedString = updateAnnotatedStringWithSpans(
                            oldAnnotatedString = textValue.annotatedString,
                            newTextFieldValue = actualText,
                            selectedColor = selectedColor,
                            colorInitial = colorInitial
                        )

                        val response = actualText.copy(
                            annotatedString = newAnnotatedString,
                            composition = null
                        )

                        onChangeTextValue(response)
                    }
                )
            }

            if (showDialog) {
                ColorPickerDialog(
                    colorInitial = colorInitial,
                    onDismissRequest = onDissmissDialog,
                    onColorSelected = { colorActual ->
                        onColorSelected(colorActual)
                    },
                    onDefaultClick = {
                        onDefaultColor()
                    }
                )
            }
        }
    }
}

fun applyColorToRange(
    oldAnnotatedString: AnnotatedString,
    actualText: TextFieldValue,
    start: Int,
    end: Int,
    color: Color
): AnnotatedString {
    val newText = actualText.text
    val builder = AnnotatedString.Builder(newText)

    val lengthDiff = newText.length - oldAnnotatedString.text.length

    // 1. Recuperar los estilos anteriores y reajustar sus posiciones al nuevo texto
    for (span in oldAnnotatedString.spanStyles) {
        var newStart = span.start
        var newEnd = span.end

        if (newStart >= start) {
            newStart += lengthDiff
        }
        if (newEnd > start) {
            newEnd += lengthDiff
        }

        if (newStart in 0..newText.length && newEnd in newStart..newText.length) {
            builder.addStyle(span.item, newStart, newEnd)
        }
    }

    // 2. Aplicar el nuevo color al rango recién insertado
    if (start in 0..end && end <= newText.length) {
        builder.addStyle(
            style = SpanStyle(color = color),
            start = start,
            end = end
        )
    }

    return builder.toAnnotatedString()
}

fun updateAnnotatedStringWithSpans(
    oldAnnotatedString: AnnotatedString,
    newTextFieldValue: TextFieldValue,
    selectedColor: Color,
    colorInitial: Color
): AnnotatedString {
    val oldText = oldAnnotatedString.text
    val newText = newTextFieldValue.text
    val lengthDiff = newText.length - oldText.length
    val cursorPosition = newTextFieldValue.selection.start
    val editPosition = (cursorPosition - lengthDiff).coerceAtLeast(0)

    val builder = AnnotatedString.Builder(newText)

    // A) PRESERVAR Y REAJUSTAR COLORES ANTERIORES
    for (span in oldAnnotatedString.spanStyles) {
        var newStart = span.start
        var newEnd = span.end

        if (lengthDiff > 0) {
            // Inserción de texto: desplaza los estilos que están después del cursor
            if (newStart >= editPosition) newStart += lengthDiff
            if (newEnd > editPosition) newEnd += lengthDiff
        } else if (lengthDiff < 0) {
            // Borrado de texto: recorta o desplaza los estilos afectados
            val deleteStart = cursorPosition
            if (newStart > deleteStart) {
                newStart = (newStart + lengthDiff).coerceAtLeast(deleteStart)
            }
            if (newEnd > deleteStart) {
                newEnd = (newEnd + lengthDiff).coerceAtLeast(deleteStart)
            }
        }

        // Mantiene el span si sigue dentro del rango válido
        if (newStart < newEnd && newStart in 0..newText.length && newEnd in 0..newText.length) {
            builder.addStyle(span.item, newStart, newEnd)
        }
    }

    // B) APLICAR EL NUEVO COLOR ÚNICAMENTE A LO RECIÉN INSERTADO
    val isDifferentColor = selectedColor.toArgb() != colorInitial.toArgb()
    if (lengthDiff > 0 && isDifferentColor) {
        val endInsert = cursorPosition.coerceAtMost(newText.length)

        if (editPosition in 0..endInsert && endInsert <= newText.length) {
            builder.addStyle(
                style = SpanStyle(color = selectedColor),
                start = editPosition,
                end = endInsert
            )
        }
    }

    return builder.toAnnotatedString()
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

fun QuestionContentUi.Text.toAnnotatedString(): AnnotatedString {
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