package com.jonathanev.review.presentation.event

sealed class RenameGuideEvent {
    data class ShowMessage(val message: String): RenameGuideEvent()
}