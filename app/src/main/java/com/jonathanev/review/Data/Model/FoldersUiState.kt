package com.jonathanev.review.Data.Model

import com.jonathanev.review.Data.Model.prueba.FolderUI

data class FoldersUiState(
    val isLoading: Boolean = false,
    val folders: List<FolderUI> = emptyList(),
    val error: String? = null
)
