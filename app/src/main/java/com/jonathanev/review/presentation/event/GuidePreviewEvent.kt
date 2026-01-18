package com.jonathanev.review.presentation.event

sealed class GuidePreviewEvent {
    data class ShowMessage(val text: String) : GuidePreviewEvent()
}