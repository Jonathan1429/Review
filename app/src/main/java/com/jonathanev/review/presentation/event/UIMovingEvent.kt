package com.jonathanev.review.presentation.event

sealed class UIMovingEvent {
    data class ShowMessage(val text: String): UIMovingEvent()
}