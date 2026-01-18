package com.jonathanev.review.presentation.event

sealed class MainUiEvent {
    data object ShowCreateFoldersError : MainUiEvent()
}
