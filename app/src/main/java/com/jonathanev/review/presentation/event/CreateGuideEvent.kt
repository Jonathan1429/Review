package com.jonathanev.review.presentation.event

sealed class CreateGuideEvent {
    data object NotQuestionBefore: CreateGuideEvent()
    data object AddMoreQuestions: CreateGuideEvent()
    data object SuccessGuideCreated: CreateGuideEvent()
    data object ErrorGuideCreated: CreateGuideEvent()
    data class ShowMessage(val text: String): CreateGuideEvent()
}