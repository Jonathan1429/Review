package com.jonathanev.review.presentation.folders.model


sealed class FolderResult {
    data class Success(val folder: FolderUiModel) : FolderResult()
    data class Error(val message: String) : FolderResult()
}