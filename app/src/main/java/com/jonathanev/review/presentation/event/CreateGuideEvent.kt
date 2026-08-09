package com.jonathanev.review.presentation.event

sealed class CreateGuideEvent {
    data class SuccessGuideCreated(val text: String): CreateGuideEvent()
    data object WithoutText: CreateGuideEvent()
    data class WithoutTextInPos(val position: Int) : CreateGuideEvent()
    data object WithoutTextQA: CreateGuideEvent()
    data class ErrorGuideCreated(val text: String) : CreateGuideEvent()
    data object QADeleted : CreateGuideEvent()
    data object CloseGuide : CreateGuideEvent()
}