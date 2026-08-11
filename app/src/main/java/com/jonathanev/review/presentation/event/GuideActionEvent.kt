package com.jonathanev.review.presentation.event

import com.jonathanev.review.presentation.model.GuideUiModel

sealed class GuideActionEvent {
    data object OpenGuide : GuideActionEvent()
    data class RenameGuide(val guideUiModel: GuideUiModel) : GuideActionEvent()
    data object MoveGuide : GuideActionEvent()
    data object GuideDeleteSuccess : GuideActionEvent()
    data class ShowMessage(override val text: String): GuideActionEvent(), UiMessageEvent
}