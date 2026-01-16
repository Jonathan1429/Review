package com.jonathanev.review.presentation.event

sealed interface UiMessageEvent {
    val text: String
}