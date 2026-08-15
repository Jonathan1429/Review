package com.jonathanev.review.presentation.state

sealed class CreatingUIState {
    data class Message(val message: String) : CreatingUIState()
    data object CreateFile: CreatingUIState()
    data object RenameFile: CreatingUIState()
    data object CreateFolder: CreatingUIState()
    data object RenameFolder : CreatingUIState()
}
