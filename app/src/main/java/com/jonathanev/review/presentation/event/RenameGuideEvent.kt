package com.jonathanev.review.presentation.event

sealed class RenameGuideEvent {
    data object ImageError: RenameGuideEvent()
    data object RenamedError: RenameGuideEvent()
    data object Success: RenameGuideEvent()
}