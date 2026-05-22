package com.jonathanev.review.presentation.event

sealed class CreateFilesEvent {
    data class ShowMessage(val message: String): CreateFilesEvent()
    data object CreatingFolder: CreateFilesEvent()
    data object RenamingFile: CreateFilesEvent()
    data object CreateFile: CreateFilesEvent()
}