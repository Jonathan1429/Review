package com.jonathanev.review.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jonathanev.review.presentation.state.CreateFoldersState
import com.jonathanev.review.presentation.state.FoldersUiState
import com.jonathanev.review.presentation.viewmodel.MainActivityViewModel
import com.jonathanev.review.ui.components.CreateFoldersPopUpContent

@Composable
fun MainActivityEntryRoute(
    viewModel: MainActivityViewModel,
    onNavWithoutFolderScreen: () -> Unit,
    onNavListFoldersScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val foldersState by viewModel.foldersState.collectAsStateWithLifecycle()

    if (foldersState is CreateFoldersState.Error) {
        Dialog(onDismissRequest = {}) {
            Box(Modifier.fillMaxSize()) {
                CreateFoldersPopUpContent(
                    modifier = Modifier.align(Alignment.Center),
                    onRetryRequest = {
                        viewModel.createFolders()
                    },
                    onDismissRequest = {
                        viewModel.onDismissErrorDialog()
                    }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.createFolders()
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            FoldersUiState.Empty -> {
                onNavWithoutFolderScreen()
            }

            FoldersUiState.Loading -> {
                // Mientras carga, no se dispara ninguna navegación
            }

            is FoldersUiState.Success -> {
                onNavListFoldersScreen()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState == FoldersUiState.Loading) {
            CircularProgressIndicator()
        }
    }
}