package com.jonathanev.review.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.presentation.event.PreviewGuideEvent
import com.jonathanev.review.presentation.model.ActiveGuideUIState
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
import com.jonathanev.review.presentation.viewmodel.PreviewViewModel
import com.jonathanev.review.ui.components.ErrorComponent
import com.jonathanev.review.ui.components.QuestionCard
import com.jonathanev.review.ui.components.singleClick
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.PreviewQuestionsProvider
import com.jonathanev.review.ui.theme.HardColorButton
import com.jonathanev.review.ui.theme.ReviewTheme

@DevicePreviews
@Composable
fun PreviewPreviewQuestionsScreen(
    //@PreviewParameter(PreviewQuestionsProvider::class) data: PreviewQuestionsProv
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
                onCreateQuestionClick = {}
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
    onCreateQuestionClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = singleClick { onCreateQuestionClick() },
                containerColor = HardColorButton
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Boton crear carpeta",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(previewQuestions.previewState) { index, question ->
                    QuestionCard(
                        question = question.question.text,
                        noTexts = previewQuestions.previewState[index].noTexts,
                        noImages = previewQuestions.previewState[index].noImages,
                        onEditingGuideClick = { onEditingGuideClick(index) },
                        onPlayGuideClick = { onPlayGuideClick(index) }
                    )
                }
            }
        }
    }
}
