package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
                        context,
                        event.error,
                        Toast.LENGTH_SHORT
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

    // 1. Estado local de la lista para renderizado continuo
    var items by remember(previewQuestions.previewState) {
        mutableStateOf(previewQuestions.previewState)
    }

    // 2. Rastreamos la posición donde inició el drag y la posición final
    var initialDragIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragIndex by remember { mutableStateOf<Int?>(null) }

    val reorderableLazyColumnState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThreshold = 120.dp,
        onMove = { from, to ->
            if (initialDragIndex == null) {
                initialDragIndex = from.index
            }

            items = items.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            currentDragIndex = to.index
        }
    )

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
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
            ) {
                itemsIndexed(
                    items = items,
                    key = { _, question -> System.identityHashCode(question) }
                ) { index, question ->
                    ReorderableItem(
                        state = reorderableLazyColumnState,
                        key = System.identityHashCode(question)
                    ) { isDragging ->
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 8.dp else 0.dp,
                            label = "elevation_animation"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation, RoundedCornerShape(12.dp))
                                // 3. Se notifica al ViewModel SOLO cuando termina el gesto completo
                                .longPressDraggableHandle(
                                    onDragStopped = {
                                        val start = initialDragIndex
                                        val end = currentDragIndex
                                        if (start != null && end != null && start != end) {
                                            onMoveQuestion(start, end)
                                        }
                                        initialDragIndex = null
                                        currentDragIndex = null
                                    }
                                )
                        ) {
                            QuestionCard(
                                question = question.question.text,
                                noTexts = question.noTexts,
                                noImages = question.noImages,
                                onEditingGuideClick = { onEditingGuideClick(index) },
                                onPlayGuideClick = { onPlayGuideClick(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}