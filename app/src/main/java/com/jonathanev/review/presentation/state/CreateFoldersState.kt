package com.jonathanev.review.presentation.state

sealed interface CreateFoldersState {
    object Idle : CreateFoldersState
    object Loading : CreateFoldersState
    object Error : CreateFoldersState
}