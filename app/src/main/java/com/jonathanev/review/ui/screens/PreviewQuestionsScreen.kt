package com.jonathanev.review.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.R
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.presentation.state.PreviewQuestionStateUi
import com.jonathanev.review.presentation.viewmodel.FragmentRepasarViewModel
import com.jonathanev.review.presentation.viewmodel.NavigationViewModel
import com.jonathanev.review.ui.components.QuestionCard
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.PreviewQuestionsProvider
import com.jonathanev.review.ui.theme.ReviewTheme
import com.jonathanev.review.ui.theme.cardStepBackground

@DevicePreviews
@Composable
fun PreviewPreviewQuestionsScreen(
    //@PreviewParameter(PreviewQuestionsProvider::class) data: PreviewQuestionsProv
    @PreviewParameter(PreviewQuestionsProvider ::class) data: PreviewQuestionStateUi
) {
    ReviewTheme {
        PreviewQuestionsScreen(data)
    }
}

@Composable
fun PreviewQuestionsRoute(
    viewModel: FragmentRepasarViewModel,
    navigationViewModel: NavigationViewModel,
    nameGuide: String
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
        previewQuestions = previewQuestions
    )
}

@Composable
fun PreviewQuestionsScreen(
    previewQuestions: PreviewQuestionStateUi
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cardStepBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.lblListQuestions),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(previewQuestions.previewState) { index, question ->
                QuestionCard(
                    question = question.question.text,
                    noTexts = previewQuestions.previewState[index].noTexts,
                    noImages = previewQuestions.previewState[index].noImages
                )
            }
        }
    }
}
