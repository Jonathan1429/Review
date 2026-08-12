package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.presentation.event.PreviewGuideEvent
import com.jonathanev.review.presentation.model.ActiveGuideUIState
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
import com.jonathanev.review.presentation.viewmodel.PreviewViewModel
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.QuestionCard
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.PreviewQuestionsProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import kotlinx.coroutines.delay
import kotlin.math.abs

@DevicePreviews
@Composable
fun PreviewPreviewQuestionsScreen(
    @PreviewParameter(PreviewQuestionsProvider::class) data: PreviewQuestionStateUi
) {
    ReviewTheme {
        PreviewQuestionsScreen(
            data,
            onEditingGuideClick = { },
            onPlayGuideClick = {},
            onCreateQuestionClick = {}
        )
    }
}

@Composable
fun PreviewQuestionsRoute(
    viewModel: PreviewViewModel,
    onEditingGuideClick: () -> Unit,
    onPlayGuideClick: () -> Unit,
    onBackNav: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    LaunchedEffect(uiState.activeGuide) {
        if (uiState.activeGuide is ActiveGuideUIState.Error) {
            Toast.makeText(
                context,
                "No se pudieron cargar los datos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.previewGuideEvent.collect { event ->
            when (event) {
                PreviewGuideEvent.Editing -> {
                    onEditingGuideClick()
                }

                PreviewGuideEvent.Review -> {
                    onPlayGuideClick()
                }

                is PreviewGuideEvent.ShowError -> {
                    Toast.makeText(
                        /* context = */ context,
                        /* text = */ event.error,
                        /* duration = */ Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    when (uiState.activeGuide) {
        ActiveGuideUIState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ActiveGuideUIState.Error -> {
            ErrorComponent(
                onRetry = viewModel::retryLoad,
                onBack = onBackNav
            )
        }

        is ActiveGuideUIState.Success -> {
            PreviewQuestionsScreen(
                previewQuestions = uiState,
                onEditingGuideClick = { position -> viewModel.editingGuide(position = position) },
                onPlayGuideClick = { position -> viewModel.reviewGuide(position = position) },
                onCreateQuestionClick = { position -> viewModel.editingGuide(position = position) },
                onMoveQuestion = { from, to -> viewModel.moveQuestion(from, to) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewQuestionsScreen(
    previewQuestions: PreviewQuestionStateUi,
    onEditingGuideClick: (Int) -> Unit,
    onPlayGuideClick: (Int) -> Unit,
    onCreateQuestionClick: (Int) -> Unit,
    onMoveQuestion: (Int, Int) -> Unit = { _, _ -> }
) {
    val lazyListState = rememberLazyListState()
    val items = previewQuestions.previewState

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var listStateForDrag by remember(items) { mutableStateOf(items) }

    val spacingPx = with(LocalDensity.current) { 16.dp.toPx() }

    // Lógica de auto-scroll cuando se arrastra cerca de los bordes
    LaunchedEffect(draggedIndex, dragOffset) {
        if (draggedIndex == null) return@LaunchedEffect

        while (true) {
            val currentIdx = draggedIndex ?: break
            val layoutInfo = lazyListState.layoutInfo
            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == currentIdx + 1 } ?: break

            val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
            val itemTop = itemInfo.offset + dragOffset
            val itemBottom = itemTop + itemInfo.size

            val threshold = 180f
            var scrollAmount = 0f

            if (itemTop < threshold) {
                val intensity = ((threshold - itemTop) / threshold).coerceIn(0f, 1f)
                scrollAmount = -intensity * 35f
            } else if (itemBottom > viewportHeight - threshold) {
                val intensity =
                    ((itemBottom - (viewportHeight - threshold)) / threshold).coerceIn(0f, 1f)
                scrollAmount = intensity * 35f
            }

            if (scrollAmount != 0f) {
                val canScroll =
                    if (scrollAmount > 0) lazyListState.canScrollForward else lazyListState.canScrollBackward
                if (!canScroll) break

                lazyListState.scrollBy(scrollAmount)
                dragOffset += scrollAmount

                // Swap durante el scroll para mantener la posición lógica
                val direction = if (scrollAmount > 0) 1 else -1
                val targetIdx = currentIdx + direction

                if (targetIdx in listStateForDrag.indices) {
                    val targetItemInfo =
                        layoutInfo.visibleItemsInfo.find { it.index == targetIdx + 1 }
                    if (targetItemInfo != null) {
                        val fullStep = targetItemInfo.size + spacingPx
                        if (abs(dragOffset) > fullStep * 0.8f) {
                            val newList = listStateForDrag.toMutableList()
                            java.util.Collections.swap(newList, currentIdx, targetIdx)
                            listStateForDrag = newList
                            draggedIndex = targetIdx
                            dragOffset -= direction * fullStep
                        }
                    }
                }
                delay(16)
            } else {
                break
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = singleClick { onCreateQuestionClick(previewQuestions.previewState.size) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.plus),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.btnAddQuestions),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header Stabilizer: Evita saltos cuando el primer item cambia de posición
                item(key = "header_stabilizer") {
                    Spacer(Modifier.height(1.dp))
                }

                itemsIndexed(
                    items = listStateForDrag,
                    key = { _, question ->
                        "${question.question.text}_${System.identityHashCode(question)}"
                    }
                ) { index, question ->
                    val isDragging = index == draggedIndex

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 10f else 1f)
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffset else 0f
                                scaleX = if (isDragging) 1.05f else 1f
                                scaleY = if (isDragging) 1.05f else 1f
                                shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                            }
                            .then(if (isDragging) Modifier else Modifier.animateItem())
                            .pointerInput(question) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { _ ->
                                        val currentIndex = listStateForDrag.indexOf(question)
                                        if (currentIndex != -1) {
                                            draggedIndex = currentIndex
                                            dragOffset = 0f
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        val currentIdx =
                                            draggedIndex ?: return@detectDragGesturesAfterLongPress

                                        // CLAMPING: Evitar que el item salga de la pantalla
                                        val layoutInfo = lazyListState.layoutInfo
                                        val viewportHeight =
                                            layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

                                        // Ajustamos el índice por el estabilizador en la posición 0
                                        val itemInfo =
                                            layoutInfo.visibleItemsInfo.find { it.index == currentIdx + 1 }

                                        if (itemInfo != null) {
                                            val currentTop = itemInfo.offset + dragOffset
                                            val currentBottom = currentTop + itemInfo.size

                                            if (currentTop < 0) {
                                                dragOffset -= currentTop
                                            } else if (currentBottom > viewportHeight) {
                                                dragOffset -= (currentBottom - viewportHeight)
                                            }
                                        }

                                        // Swap logic
                                        var foundSwap = true
                                        while (foundSwap) {
                                            foundSwap = false
                                            val direction = if (dragOffset > 0) 1 else -1
                                            val targetIdx = draggedIndex!! + direction

                                            if (targetIdx in listStateForDrag.indices) {
                                                val targetItemInfo =
                                                    layoutInfo.visibleItemsInfo.find { it.index == targetIdx + 1 }
                                                val currentItemInfo =
                                                    layoutInfo.visibleItemsInfo.find { it.index == draggedIndex!! + 1 }

                                                val stepSize =
                                                    targetItemInfo?.size ?: currentItemInfo?.size
                                                    ?: 300
                                                val fullStep = stepSize + spacingPx

                                                if (abs(dragOffset) > fullStep * 0.8f) {
                                                    val newList = listStateForDrag.toMutableList()
                                                    java.util.Collections.swap(
                                                        newList,
                                                        draggedIndex!!,
                                                        targetIdx
                                                    )
                                                    listStateForDrag = newList
                                                    draggedIndex = targetIdx
                                                    dragOffset -= direction * fullStep
                                                    foundSwap = true
                                                }
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedIndex?.let { finalIdx ->
                                            val originalIndex =
                                                items.indexOf(listStateForDrag[finalIdx])
                                            if (originalIndex != -1 && finalIdx != originalIndex) {
                                                onMoveQuestion(originalIndex, finalIdx)
                                            }
                                        }
                                        draggedIndex = null
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggedIndex = null
                                        dragOffset = 0f
                                        listStateForDrag = items
                                    }
                                )
                            }
                    ) {
                        QuestionCard(
                            question = question.question.text,
                            noTexts = question.noTexts,
                            noImages = question.noImages,
                            onEditingGuideClick = {
                                onEditingGuideClick(
                                    listStateForDrag.indexOf(
                                        question
                                    )
                                )
                            },
                            onPlayGuideClick = { onPlayGuideClick(listStateForDrag.indexOf(question)) }
                        )
                    }
                }
            }
        }
    }
}
