package com.jonathanev.review.presentation.event

sealed class StateGuideActionEvent {
    data class ShowMessage(val text: String) : StateGuideActionEvent()
    data object ExistFile : StateGuideActionEvent()
    data object GuideDeleteSuccess : StateGuideActionEvent()
}