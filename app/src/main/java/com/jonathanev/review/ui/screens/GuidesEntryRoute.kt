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
        if (uiState == EntryGuidesUiState.Empty) {
            onNavigateWithoutFilesScreen()
        }

        if (uiState == EntryGuidesUiState.HasGuides) {
            onNavigateListGuidesRoute()
        }
    }

    when (uiState) {
        EntryGuidesUiState.Empty -> {
            Box(modifier = Modifier.fillMaxSize())
        }

        EntryGuidesUiState.HasGuides -> {
            Box(modifier = Modifier.fillMaxSize())
        }

        EntryGuidesUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}