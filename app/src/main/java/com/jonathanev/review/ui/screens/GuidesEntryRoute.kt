package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    LaunchedEffect(uiState) {
        when (uiState) {
            EntryGuidesUiState.HasGuides -> {
                onNavigateListGuidesRoute()
            }

            EntryGuidesUiState.Empty -> {
                onNavigateWithoutFilesScreen()
            }

            EntryGuidesUiState.Loading -> {
                /* Esperar a que evalúe el disco */
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState == EntryGuidesUiState.Loading) {
            CircularProgressIndicator()
        }
    }
}