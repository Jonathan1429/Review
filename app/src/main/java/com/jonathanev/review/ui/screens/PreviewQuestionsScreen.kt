package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
import com.jonathanev.review.presentation.viewmodel.FragmentRepasarViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.ui.components.QuestionCard
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.PreviewQuestionsProvider
import com.jonathanev.review.ui.theme.ColorBotones
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
    viewModel: FragmentRepasarViewModel,
    navigationViewModel: NavigationViewModel,
    nameGuide: String,
    onEditingGuideClick: (Int) -> Unit,
    onPlayGuideClick: () -> Unit
) {
    val relativeGuidePath =
        navigationViewModel.relativeGuidePath.collectAsStateWithLifecycle().value
    //val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val previewQuestions = viewModel.uiStatePreview.collectAsStateWithLifecycle().value

    /*val noImages = viewModel.imageList.collectAsStateWithLifecycle().value.size
    val noTexts = viewModel.textList.collectAsStateWithLifecycle().value.size*/

    LaunchedEffect(Unit) {
        viewModel.uploadCachedGuides()
        viewModel.getObtenerDatosXML(
            folderId = nameGuide,
            relativeGuidePath = RelativeGuidePath(relativeGuidePath)
        )
    }

    PreviewQuestionsScreen(
        previewQuestions = previewQuestions,
        onEditingGuideClick = { position -> onEditingGuideClick(position) },
        onPlayGuideClick = onPlayGuideClick,
        onCreateQuestionClick = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewQuestionsScreen(
    previewQuestions: PreviewQuestionStateUi,
    onEditingGuideClick: (Int) -> Unit,
    onPlayGuideClick: () -> Unit,
    onCreateQuestionClick: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateQuestionClick,
                containerColor = ColorBotones
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
                        onPlayGuideClick = onPlayGuideClick
                    )
                }
            }
        }
    }
}
