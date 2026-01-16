package com.jonathanev.review.presentation.event

sealed class GuideReviewEvent {
    data object NotQuestionBefore : GuideReviewEvent()
    data object RestartGuide : GuideReviewEvent()
    data class ShowMessage(override val text: String) : GuideReviewEvent(), UiMessageEvent
}