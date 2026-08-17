package com.jonathanev.review.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.event.CreateGuideEvent
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.presentation.state.GuideScreenUiState
import com.jonathanev.review.presentation.viewmodel.SharedFragmentCreateFileViewModel
import com.jonathanev.review.ui.components.AssetCarouselViewer
import com.jonathanev.review.ui.components.CustomAlertDialog
import com.jonathanev.review.ui.components.CustomTopBar
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.FilterTypeItem
import com.jonathanev.review.ui.components.QASelectType
import com.jonathanev.review.ui.components.ShowDeletePopUp
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.model.QAType
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProv
import com.jonathanev.review.ui.preview.providers.StudyGuideScreenProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import kotlin.math.roundToInt

@DevicePreviews
@Composable
fun PreviewStudyGuideScreen(
    @PreviewParameter(StudyGuideScreenProvider::class) data: StudyGuideScreenProv
) {
    ReviewTheme {
        FillingGuideScreen(
            cardType = data.typeSelected,
            typeForSelected = data.typeForSelected,
            mediaSelected = data.mediaSelected,
            mediaForSelected = data.mediaForSelected,
            actualQuestion = data.actualQuestion,
            totalQuestions = data.totalQuestions,
            listTypeMedia = data.listTypeMedia,
            guideContext = data.guideContext,
            currentPosContent = 0,
            showDialogDeleteQuestion = data.showDialogDeleteQuestion,
            showDialogRepeatGuide = data.showDialogRepeatGuide,
            onDissmissDialogRepeatGuide = {},
            onConfirmDialogRepeatGuide = {},
            onContinueDialogDeleteQuestionClick = {},
            onBackQuestionClick = {},
            onNextQuestionClick = {},
            onDeleteQuestionClick = { },
            onCardTypeClicked = {},
            onFilterTypeClicked = {},
            onOpenAssetClick = { _, _ -> },
            onDeleteItemClick = { _, _ -> },
            onAddAssetClick = {},
            onAddQuestion = {},
            onCloseGuide = {},
            onCurrentPosContent = {},
            onDismissRequest = {}
        )
    }
}

@Composable
fun StudyGuideRoute(
    viewModel: SharedFragmentCreateFileViewModel,
    onOpenAssetClick: (QuestionContentUi, posItem: Int) -> Unit,
    onAddAssetClick: (ContentType, posItem: Int) -> Unit,
    onActionGuideNone: () -> Unit,
    onCloseGuide: () -> Unit
) {
    val context = LocalContext.current
    val typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER)
    val mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE)
    var showExitDialog by remember { mutableStateOf(false) }

    // INTERCEPTA EL BOTÓN/GESTO "ATRÁS" DEL SISTEMA
    BackHandler {
        if (viewModel.isEditingOrCreating() && viewModel.hasChangesInGuide()) {
            showExitDialog = true
        } else {
            viewModel.onDiscardGuide()
            onCloseGuide()
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN AL DAR ATRÁS EN MODO CREACIÓN/EDICIÓN
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = "¿Descartar cambios?") },
            text = { Text(text = "Si sales ahora, se perderán las modificaciones.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        viewModel.onDiscardGuide()
                        onCloseGuide()
                    }
                ) {
                    Text(text = "Descartar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(text = "Seguir editando", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

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
                onBack = onCloseGuide
            )
        }

        is GuideScreenUiState.Success -> {
            val cardType = state.qAType
            val mediaSelected = state.mediaSelected
            val currentPosContent = state.contadorContenido

            val totalQuestions = state.preguntas.size
            val actualQuestion = state.contadorPregunta + 1
            val textList by viewModel.textList.collectAsStateWithLifecycle()
            val imageList by viewModel.imageList.collectAsStateWithLifecycle()

            val listTypeMedia = if (state.mediaSelected == ContentType.TEXT) {
                textList
            } else {
                imageList
            }

            LaunchedEffect(Unit) {
                viewModel.createGuideEvent.collect { event ->
                    when (event) {
                        CreateGuideEvent.WithoutText ->
                            showToast("Debes tener al menos un texto", context)

                        CreateGuideEvent.WithoutTextQA ->
                            showToast(
                                "Debes tener al menos un texto en pregunta y respuesta",
                                context
                            )

                        is CreateGuideEvent.WithoutTextInPos -> {
                            showToast(
                                "Revisa la pregunta ${event.position} - Debes tener al menos un texto en pregunta/respuesta",
                                context
                            )
                        }

                        is CreateGuideEvent.ErrorGuideCreated -> {
                            showToast(event.text, context)
                            onCloseGuide()
                        }

                        is CreateGuideEvent.SuccessGuideCreated -> {
                            showToast(event.text, context)
                            onCloseGuide()
                        }

                        CreateGuideEvent.QADeleted -> {
                            showToast("Se ha eliminado la pregunta y respuesta", context)
                        }

                        CreateGuideEvent.CloseGuide -> {
                            onCloseGuide()
                        }

                        CreateGuideEvent.ErrorMoveContent -> {
                            showToast("No se pudo mover el contenido", context)
                        }
                    }
                }
            }

            FillingGuideScreen(
                cardType = cardType,
                typeForSelected = typeForSelected,
                mediaSelected = mediaSelected,
                mediaForSelected = mediaForSelected,
                actualQuestion = actualQuestion,
                totalQuestions = totalQuestions,
                listTypeMedia = listTypeMedia,
                guideContext = state.guideContext,
                currentPosContent = currentPosContent,
                showDialogDeleteQuestion = state.showDialogDeleteQuestion,
                showDialogRepeatGuide = state.showDialogRepeatGuide,
                onDissmissDialogRepeatGuide = viewModel::onDismissDialogRepeatGuide,
                onConfirmDialogRepeatGuide = viewModel::restartGuide,
                onContinueDialogDeleteQuestionClick = { isChecked ->
                    viewModel.onConfirmDeleteQuestion(dontAskAgain = isChecked)
                },
                onBackQuestionClick = {
                    viewModel.previousQuestion()
                },
                onNextQuestionClick = {
                    viewModel.onNextQuestionRequested(
                        actualQuestion = actualQuestion,
                        totalQuestions = totalQuestions
                    )
                },
                onDeleteQuestionClick = viewModel::onDeleteQuestionRequested,
                onDeleteItemClick = { typeContent, positionItem ->
                    when (typeContent) {
                        is QuestionContentUi.Image -> {
                            viewModel.deleteImage(positionItem)
                        }

                        QuestionContentUi.None -> onActionGuideNone()
                        is QuestionContentUi.Text -> {
                            viewModel.deleteText(positionItem)
                        }
                    }
                },
                onCardTypeClicked = { cardTypeClicked ->
                    viewModel.onCardTypeChanged(cardTypeClicked)
                },
                onFilterTypeClicked = { filterTypeClicked ->
                    viewModel.onFilterTypeChanged(filterTypeClicked = filterTypeClicked)
                },
                onOpenAssetClick = { typeContent, posItem ->
                    onOpenAssetClick(
                        typeContent,
                        posItem
                    )
                },
                onAddAssetClick = { posItem -> onAddAssetClick(mediaSelected, posItem) },
                onAddQuestion = viewModel::addNextQuestion,
                onCloseGuide = {
                    when (state.guideContext) {
                        is GuideContext.Creating -> viewModel.saveGuide()
                        is GuideContext.Editing -> viewModel.saveGuide()
                        else -> onCloseGuide()
                    }
                },
                onCurrentPosContent = { position ->
                    viewModel.updatePosContent(position)
                },
                onDismissRequest = viewModel::onDismissDialogDeleteQuestion,
                onMoveItem = { from, to ->
                    viewModel.onMoveItem(from, to)
                },
                onEditGuideClick = viewModel::switchToEditMode,
                hasContentQA = { type ->
                    val question = state.preguntas.getOrNull(state.contadorPregunta)
                    val answer = state.respuestas.getOrNull(state.contadorPregunta)
                    if (type == QAType.QUESTION) {
                        question?.content?.isNotEmpty() ?: false
                    } else {
                        answer?.content?.isNotEmpty() ?: false
                    }
                },
                hasContentMedia = { type ->
                    val currentPart = if (state.qAType == QAType.QUESTION) {
                        state.preguntas.getOrNull(state.contadorPregunta)
                    } else {
                        state.respuestas.getOrNull(state.contadorPregunta)
                    }
                    currentPart?.content?.any { content ->
                        when (type) {
                            ContentType.TEXT -> content is QuestionContentUi.Text
                            ContentType.IMAGE -> content is QuestionContentUi.Image
                        }
                    } ?: false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillingGuideScreen(
    cardType: QAType,
    typeForSelected: List<QAType>,
    mediaSelected: ContentType,
    mediaForSelected: List<ContentType>,
    actualQuestion: Int,
    totalQuestions: Int,
    listTypeMedia: List<QuestionContentUi>,
    guideContext: GuideContext,
    showDialogDeleteQuestion: Boolean,
    currentPosContent: Int,
    showDialogRepeatGuide: Boolean,
    onDissmissDialogRepeatGuide: () -> Unit,
    onConfirmDialogRepeatGuide: () -> Unit,
    onContinueDialogDeleteQuestionClick: (Boolean) -> Unit,
    onBackQuestionClick: () -> Unit,
    onNextQuestionClick: () -> Unit,
    onDeleteQuestionClick: () -> Unit,
    onCardTypeClicked: (QAType) -> Unit,
    onFilterTypeClicked: (ContentType) -> Unit,
    onOpenAssetClick: (QuestionContentUi, posItem: Int) -> Unit,
    onDeleteItemClick: (typeContent: QuestionContentUi, positionItem: Int) -> Unit,
    onAddAssetClick: (posItem: Int) -> Unit,
    onAddQuestion: () -> Unit,
    onCloseGuide: () -> Unit,
    onCurrentPosContent: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    onMoveItem: (Int, Int) -> Unit = { _, _ -> },
    onEditGuideClick: () -> Unit = {},
    hasContentQA: (QAType) -> Boolean = { false },
    hasContentMedia: (ContentType) -> Boolean = { false }
) {
    var showPlusOneAnimation by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }

    var actualQuestionOffset by remember { mutableStateOf(Offset.Zero) }
    var totalQuestionsOffset by remember { mutableStateOf(Offset.Zero) }
    var containerOffset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        floatingActionButton = {
            FloatingActionButtons(
                guideContext = guideContext,
                onAddQuestion = {
                    if (!isAnimating) {
                        isAnimating = true
                        showPlusOneAnimation = true
                    }
                },
                onCloseGuide = onCloseGuide,
                onEditGuide = onEditGuideClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    containerOffset = it.positionInWindow()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                CustomTopBar(
                    actualQuestion = actualQuestion,
                    totalQuestions = totalQuestions,
                    guideContext = guideContext,
                    onDeleteQuestionClick = onDeleteQuestionClick,
                    onBackQuestionClick = onBackQuestionClick,
                    onNextQuestionClick = onNextQuestionClick,
                    onActualQuestionPositioned = { actualQuestionOffset = it },
                    onTotalQuestionsPositioned = { totalQuestionsOffset = it }
                )
                QASelectType(
                    typeForSelected = typeForSelected,
                    cardType = cardType,
                    hasContent = hasContentQA,
                    onCardTypeClicked = { cardTypeClicked ->
                        onCardTypeClicked(cardTypeClicked)
                    })
                FilterTypeItem(
                    mediaForSelected = mediaForSelected,
                    mediaSelected = mediaSelected,
                    hasContent = hasContentMedia,
                    onFilterTypeClicked = { filterTypeClicked ->
                        onFilterTypeClicked(filterTypeClicked)
                    })

                AssetCarouselViewer(
                    assets = listTypeMedia,
                    mediaForSelected = mediaSelected,
                    guideContext = guideContext,
                    currentPosContent = currentPosContent,
                    onAddAssetClick = { posItem -> onAddAssetClick(posItem) },
                    onDeleteItemClick = { typeContent, positionItem ->
                        onDeleteItemClick(
                            typeContent,
                            positionItem
                        )
                    },
                    onOpenAssetClick = { typeContent, posItem ->
                        onOpenAssetClick(
                            typeContent,
                            posItem
                        )
                    },
                    onCurrentPosContent = { position -> onCurrentPosContent(position) },
                    onMoveItem = { from, to -> onMoveItem(from, to) }
                )
            }

            PlusOneAnimation(
                visible = showPlusOneAnimation,
                targetActual = actualQuestionOffset - containerOffset,
                targetTotal = totalQuestionsOffset - containerOffset,
                onAnimationFinish = {
                    showPlusOneAnimation = false
                    onAddQuestion()
                    isAnimating = false
                }
            )
        }

        if (showDialogDeleteQuestion) {
            Dialog(onDismissRequest = {}) {
                Box(
                    Modifier.fillMaxSize()
                ) {
                    ShowDeletePopUp(
                        modifier = Modifier.align(Alignment.Center),
                        onContinueClick = { isChecked ->
                            onContinueDialogDeleteQuestionClick(isChecked)
                        },
                        onDismissRequest = onDismissRequest
                    )
                }
            }
        }

        if (showDialogRepeatGuide) {
            Dialog(onDismissRequest = {}) {
                Box(
                    Modifier.fillMaxSize()
                ) {
                    CustomAlertDialog(
                        modifier = Modifier.align(Alignment.Center),
                        onDismissRequest = onDissmissDialogRepeatGuide,
                        onConfirm = onConfirmDialogRepeatGuide,
                        title = stringResource(R.string.lblTitleRepeatGuide),
                        message = stringResource(R.string.lblDescriptionRepeatGuide)
                    )
                }
            }

        }
    }
}

@Composable
private fun FloatingActionButtons(
    guideContext: GuideContext,
    onAddQuestion: () -> Unit,
    onCloseGuide: () -> Unit,
    onEditGuide: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (guideContext is GuideContext.Browsing) {
            FloatingActionButton(
                onClick = singleClick { onEditGuide() },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "Editar guía",
                    tint = Color.White
                )
            }
        }

        if (guideContext !is GuideContext.Browsing) {
            FloatingActionButton(
                onClick = singleClick { onAddQuestion() },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                NewQuestionIcon(modifier = Modifier.padding(8.dp))
            }
        }

        val painter =
            if (guideContext !is GuideContext.Browsing)
                android.R.drawable.ic_menu_save
            else
                R.drawable.ic_success
        val text =
            if (guideContext !is GuideContext.Browsing)
                stringResource(R.string.btnGuardarGuia)
            else
                stringResource(R.string.lblCloseGuide)
        ExtendedFloatingActionButton(
            onClick = singleClick { onCloseGuide() },
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    modifier = Modifier.size(45.dp),
                    painter = painterResource(painter),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            text = {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        )
    }
}

@Composable
private fun NewQuestionIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.question_solid_full),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.TopEnd)
                .background(
                    Color.White,
                    CircleShape
                )
                .padding(1.dp)
        )
    }
}

@Composable
private fun PlusOneAnimation(
    visible: Boolean,
    targetActual: Offset,
    targetTotal: Offset,
    onAnimationFinish: () -> Unit
) {
    if (!visible) return

    val configuration = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current

    val screenWidth = with(density) { configuration.width.dp.toPx() }
    val screenHeight = with(density) { configuration.height.dp.toPx() }

    val animProgress = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }
    var showPulse by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )

        showPulse = true
        pulseScale.animateTo(
            targetValue = 2f,
            animationSpec = tween(durationMillis = 300)
        )
        onAnimationFinish()
    }

    // Distancia vertical debajo del número
    val verticalGap = with(density) { 25.dp.toPx() }
    val finalActual = targetActual + Offset(0f, verticalGap)
    val finalTotal = targetTotal + Offset(0f, verticalGap)

    val alpha = if (animProgress.value < 0.8f) {
        (animProgress.value / 0.2f).coerceAtMost(1f)
    } else {
        (1f - animProgress.value) / 0.2f
    }

    Box(Modifier.fillMaxSize()) {
        if (!showPulse) {
            val startX = screenWidth * 0.85f
            val startY = screenHeight * 0.85f

            // +1 "Actual"
            MovingPlusOne(
                startX = startX,
                startY = startY,
                endX = finalActual.x,
                endY = finalActual.y,
                progress = animProgress.value,
                alpha = alpha,
                rotation = -20f * (1f - animProgress.value)
            )

            // +1 "Total"
            MovingPlusOne(
                startX = startX,
                startY = startY,
                endX = finalTotal.x,
                endY = finalTotal.y,
                progress = animProgress.value,
                alpha = alpha,
                rotation = 20f * (1f - animProgress.value)
            )
        } else {
            ImpactText(x = finalActual.x, y = finalActual.y, scale = pulseScale.value)
            ImpactText(x = finalTotal.x, y = finalTotal.y, scale = pulseScale.value)
        }
    }
}

@Composable
private fun MovingPlusOne(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    progress: Float,
    alpha: Float,
    rotation: Float
) {
    val currentX = startX + (endX - startX) * progress
    val currentY = startY + (endY - startY) * progress
    var size by remember { mutableStateOf(IntSize.Zero) }

    Text(
        text = "+1",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .onSizeChanged { size = it }
            .offset {
                IntOffset(
                    (currentX - size.width / 2).roundToInt(),
                    (currentY - size.height / 2).roundToInt()
                )
            }
            .graphicsLayer(
                alpha = alpha,
                scaleX = 0.6f + progress * 0.4f,
                scaleY = 0.6f + progress * 0.4f,
                rotationZ = rotation
            )
    )
}

@Composable
private fun ImpactText(x: Float, y: Float, scale: Float) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .onSizeChanged { size = it }
            .offset {
                IntOffset(
                    (x - size.width / 2).roundToInt(),
                    (y - size.height / 2).roundToInt()
                )
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = 1f - (scale - 1f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+1",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun showToast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}
