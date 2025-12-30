package com.jonathanev.review.presentation.state

sealed class CreatingFileUiState {
    data class Message(val message: String) : CreatingFileUiState()
    data class ContinuedProcess(val name: String, val description: String) : CreatingFileUiState()
    data class FileUiStateExisted(val name: String, val description: String): CreatingFileUiState()
}