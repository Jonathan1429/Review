package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.QuestionItem

sealed class AnswerState {
    data object Empty : AnswerState()
    data class Filled(val item: QuestionItem) : AnswerState()
}

data class QAItem(
    val question: QuestionItem,
    val answer: AnswerState = AnswerState.Empty,
)
