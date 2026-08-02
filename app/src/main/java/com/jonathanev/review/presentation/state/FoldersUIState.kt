package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.FolderUiModel

sealed interface FoldersUiState {
    data object Loading : FoldersUiState
    data object Empty : FoldersUiState
    data class Success(val folders: List<FolderUiModel>) : FoldersUiState
}