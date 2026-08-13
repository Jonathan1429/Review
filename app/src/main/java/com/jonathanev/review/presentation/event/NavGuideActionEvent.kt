package com.jonathanev.review.presentation.event

import com.jonathanev.review.presentation.model.GuideUiModel

sealed class NavGuideActionEvent {
    data object OpenNavGuide : NavGuideActionEvent()
    data class RenameNavGuide(val guideUiModel: GuideUiModel) : NavGuideActionEvent()
    data object MoveNavGuide : NavGuideActionEvent()
}