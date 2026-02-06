package com.jonathanev.review.presentation.event

sealed class CreateGuideEvent {
    data object NotQuestionBefore: CreateGuideEvent()
    data object AddMoreQuestions: CreateGuideEvent()
    data class SuccessGuideCreated(val text: String): CreateGuideEvent()
    data object ErrorGuideCreated: CreateGuideEvent()
    data class ShowMessage(val text: String): CreateGuideEvent()
}