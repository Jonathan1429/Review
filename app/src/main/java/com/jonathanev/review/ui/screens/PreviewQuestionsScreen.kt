package com.jonathanev.review.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.SavingStatus
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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.resetNavigationFlag()
    }

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
        viewModel.retryLoad()
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
                uiState = uiState,
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
    uiState: PreviewQuestionStateUi,
    onEditingGuideClick: (Int) -> Unit,
    onPlayGuideClick: (Int) -> Unit,
    onCreateQuestionClick: (Int) -> Unit,
    onMoveQuestion: (Int, Int) -> Unit = { _, _ -> }
) {
    val lazyListState = rememberLazyListState()

    val reorderableLazyColumnState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThreshold = 120.dp,
        onMove = { from, to ->
            onMoveQuestion(from.index, to.index)
        }
    )

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = singleClick { onCreateQuestionClick(uiState.previewState.size) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
            ) {
                itemsIndexed(
                    items = uiState.previewState,
                    key = { _, question -> question.id }
                ) { index, question ->
                    val cardShape = RoundedCornerShape(16.dp)

                    ReorderableItem(
                        state = reorderableLazyColumnState,
                        key = question.id
                    ) { isDragging ->
                        val elevation by animateDpAsState(
                            targetValue = if (isDragging) 12.dp else 0.dp,
                            label = "elevation_animation"
                        )
                        val horizontalPaddingAnimation by animateDpAsState(
                            targetValue = if (isDragging) 0.dp else 16.dp,
                            label = "horizontal_padding_animation"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPaddingAnimation)
                                .shadow(
                                    elevation = elevation,
                                    shape = cardShape,
                                    clip = false,
                                    ambientColor = Color.Black.copy(alpha = 0.5f),
                                    spotColor = Color.Black.copy(alpha = 0.5f)
                                )
                                .clip(cardShape)
                                .longPressDraggableHandle()
                        ) {
                            QuestionCard(
                                modifier = Modifier.fillMaxWidth(),
                                question = question.question.text,
                                noTexts = question.noTexts,
                                noImages = question.noImages,
                                indexLabel = "Q${index + 1}",
                                onEditingGuideClick = { onEditingGuideClick(index) },
                                onPlayGuideClick = { onPlayGuideClick(index) }
                            )
                        }
                    }
                }
            }

            // Indicador flotante de guardado arriba a la derecha
            AnimatedVisibility(
                visible = uiState.savingStatus != SavingStatus.IDLE,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (uiState.savingStatus) {
                            SavingStatus.SAVING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Guardando...",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            SavingStatus.SAVED -> {
                                Text(
                                    text = "Guardado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            SavingStatus.ERROR -> {
                                Text(
                                    text = "Error al guardar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            SavingStatus.IDLE -> {}
                        }
                    }
                }
            }
        }
    }
}