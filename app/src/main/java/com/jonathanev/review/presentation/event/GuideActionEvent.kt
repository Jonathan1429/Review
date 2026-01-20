package com.jonathanev.review.presentation.event

sealed class GuideActionEvent {
    data class Success(override val text: String): GuideActionEvent(), UiMessageEvent
    data class ShowMessage(override val text: String): GuideActionEvent(), UiMessageEvent
}