package com.jonathanev.review.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.event.CreateGuideEvent
import com.jonathanev.review.presentation.model.ColorRangeUi
import com.jonathanev.review.presentation.model.QuestionContentMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.model.SpanPalabraModel
import com.jonathanev.review.presentation.state.GuideScreenUiState
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.ColorPickerDialog
import com.jonathanev.review.ui.components.CustomAlertDialog
import com.jonathanev.review.ui.components.CustomBoxCreateText
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.OptionsCreateText
import com.jonathanev.review.ui.mapper.toInt
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.CreateTextScreenProv
import com.jonathanev.review.ui.preview.providers.CreateTextScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground

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

            val pagerState = rememberPagerState(initialPage = posItem) {
                if (questionContentMode == QuestionContentMode.CREATING) 1 else textList.size
            }

            // Sync with ViewModel when page changes
            LaunchedEffect(pagerState.currentPage) {
                if (questionContentMode == QuestionContentMode.EDITING) {
                    viewModel.updatePosContent(pagerState.currentPage)
                }
                
                // Clear draft before initializing for new page to avoid showing old draft
                viewModel.clearTextDraft()

                val itemAtPage = if (questionContentMode == QuestionContentMode.CREATING) {
                    QuestionContentUi.Text("", emptyList())
                } else {
                    textList.getOrNull(pagerState.currentPage) ?: QuestionContentUi.Text(
                        "",
                        emptyList()
                    )
                }
                viewModel.initTextDraft(
                    initialContent = itemAtPage,
                    isEditing = questionContentMode == QuestionContentMode.EDITING
                )
            }

            val colorInitial = MaterialTheme.colorScheme.onSurface
            val colorSelected = state.colorType.toInt(isDark)
            var selectedColorInt by remember(colorSelected) {
                mutableIntStateOf(colorSelected)
            }
            val selectedColor = Color(selectedColorInt)

            val textValueState by viewModel.draftTextValue.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.updateItemTrigger.collect {
                    onSaveText()
                }
            }

            val onBackAction = {
                val currentText = textValueState?.text ?: ""
                val itemAtPage = if (questionContentMode == QuestionContentMode.CREATING) {
                    QuestionContentUi.Text("", emptyList())
                } else {
                    textList.getOrNull(pagerState.currentPage) ?: QuestionContentUi.Text("", emptyList())
                }

                val currentDraft = textValueState?.let {
                    QuestionContentUi.Text(
                        text = it.text,
                        colorRanges = it.annotatedString.spanStyles.mapNotNull { span ->
                            if (span.item.color != Color.Unspecified) {
                                ColorRangeUi(span.start, span.end, span.item.color.toArgb())
                            } else null
                        }
                    )
                }

                val hasChanges = if (questionContentMode == QuestionContentMode.CREATING) {
                    currentText.isNotEmpty()
                } else {
                    currentDraft != null && (currentDraft.text != itemAtPage.text || currentDraft.colorRanges != itemAtPage.colorRanges)
                }

                if (hasChanges) {
                    viewModel.onBackFromEditor()
                } else {
                    viewModel.clearTextDraft()
                    onBackNav()
                }
            }

            BackHandler(onBack = onBackAction)

            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { padding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    userScrollEnabled = questionContentMode == QuestionContentMode.EDITING,
                    beyondViewportPageCount = 1,
                    key = { page ->
                        val item = if (questionContentMode == QuestionContentMode.CREATING) {
                            null
                        } else {
                            textList.getOrNull(page)
                        }
                        when (item) {
                            is QuestionContentUi.Text -> "txt_${item.text.hashCode()}_$page"
                            else -> "page_$page"
                        }
                    }
                ) { page ->
                    val itemAtPage = remember(textList, page, questionContentMode) {
                        if (questionContentMode == QuestionContentMode.CREATING) {
                            QuestionContentUi.Text("", emptyList())
                        } else {
                            textList.getOrNull(page) ?: QuestionContentUi.Text("", emptyList())
                        }
                    }

                    val textValueAtPage = if (page == pagerState.currentPage) {
                        textValueState ?: remember(itemAtPage) {
                            TextFieldValue(annotatedString = itemAtPage.toAnnotatedString())
                        }
                    } else {
                        remember(itemAtPage) {
                            TextFieldValue(annotatedString = itemAtPage.toAnnotatedString())
                        }
                    }

                    TextEditorContent(
                        guideContext = state.guideContext,
                        colorInitial = colorInitial,
                        selectedColor = selectedColor,
                        textValue = textValueAtPage,
                        showDialog = state.showDialogColor,
                        onSaveText = { text, colors ->
                            viewModel.addTextContent(
                                textWithLabels = text,
                                listSpans = colors,
                                questionContentMode = questionContentMode
                            )
                        },
                        onClearColorClick = {
                            viewModel.clearColorsFromDraft()
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
                        onBackNav = onBackAction
                    )
                }
            }

            if (state.showDialogDiscardDraft) {
                Dialog(onDismissRequest = viewModel::onDismissDiscardDraft) {
                    CustomAlertDialog(
                        title = stringResource(R.string.lblDiscardChangesTitle),
                        message = stringResource(R.string.lblDiscardChangesMessage),
                        onDismissRequest = viewModel::onDismissDiscardDraft,
                        onConfirm = {
                            viewModel.onConfirmDiscardDraft()
                            onBackNav()
                        }
                    )
                }
            }
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
        Box(
            modifier = Modifier
                .padding(padding)
        ) {
            TextEditorContent(
                guideContext = guideContext,
                onSaveText = onSaveText,
                colorInitial = colorInitial,
                selectedColor = selectedColor,
                textValue = textValue,
                showDialog = showDialog,
                onClearColorClick = onClearColorClick,
                onShowColorDialog = onShowColorDialog,
                onChangeTextValue = onChangeTextValue,
                onDissmissDialog = onDissmissDialog,
                onColorSelected = onColorSelected,
                onDefaultColor = onDefaultColor,
                onBackNav = onBackNav
            )
        }
    }
}

@Composable
fun TextEditorContent(
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(42.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardStepBackground
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .imePadding()
        ) {
            OptionsCreateText(
                guideContext = guideContext,
                textValue = textValue.annotatedString,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textValue = textValue,
                hint = textValue.text.isNotEmpty(),
                readOnly = guideContext is GuideContext.Browsing,
                selectedColor = selectedColor,
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

    if (text.isEmpty()) {
        onSaveContent(text, emptyList())
        return SpanPalabraModel()
    }

    // 1. Creamos un mapa/array del tamaño del texto para evaluar el color exacto de cada carácter
    val characterColors = Array(text.length) { Color.Unspecified }

    // 2. Aplicamos los spans. Si un rango nuevo cae sobre un color distinto, detectamos sobreescritura
    spanStyles.forEach { range ->
        if (range.item.color != Color.Unspecified) {
            val start = range.start.coerceIn(0, text.length)
            val end = range.end.coerceIn(0, text.length)

            for (i in start until end) {
                if (characterColors[i] != Color.Unspecified && characterColors[i] != range.item.color) {
                    isDoubleColors = true
                }
                characterColors[i] = range.item.color
            }
        }
    }

    // 3. Recorremos el array e identificamos bloques continuos del mismo color
    var currentStart = -1
    var currentColor = Color.Unspecified

    for (i in text.indices) {
        val colorAtChar = characterColors[i]

        if (colorAtChar != currentColor) {
            // Guardamos el bloque acumulado previo si tenía color válido
            if (currentColor != Color.Unspecified && currentStart != -1) {
                listSpans.add(
                    ColorRangeUi(
                        start = currentStart,
                        end = i,
                        color = currentColor.toArgb()
                    )
                )
            }
            // Iniciamos un nuevo bloque
            currentStart = if (colorAtChar != Color.Unspecified) i else -1
            currentColor = colorAtChar
        }
    }

    // 4. Guardamos el último bloque activo al llegar al final del texto
    if (currentColor != Color.Unspecified && currentStart != -1) {
        listSpans.add(
            ColorRangeUi(
                start = currentStart,
                end = text.length,
                color = currentColor.toArgb()
            )
        )
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