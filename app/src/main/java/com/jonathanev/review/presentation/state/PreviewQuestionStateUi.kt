package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.ActiveGuideUIState
import com.jonathanev.review.presentation.model.PreviewQuestionUi

data class PreviewQuestionStateUi(
    val activeGuide: ActiveGuideUIState = ActiveGuideUIState.Loading,
    val previewState: List<PreviewQuestionUi>
)