package com.jonathanev.review.ui.screens

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.presentation.state.EntryGuidesUiState
import com.jonathanev.review.presentation.viewmodel.FragReviewEntryViewModel

@Composable
fun GuidesEntryRoute(
    onNavigateListGuidesRoute: () -> Unit,
    onNavigateWithoutFilesScreen: () -> Unit,
    viewModel: FragReviewEntryViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        EntryGuidesUiState.Empty -> {
            onNavigateWithoutFilesScreen()
        }

        EntryGuidesUiState.HasGuides -> {
            onNavigateListGuidesRoute()
        }

        EntryGuidesUiState.Loading -> {
            CircularProgressIndicator()
        }
    }
}