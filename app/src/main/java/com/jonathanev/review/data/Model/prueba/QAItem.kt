package com.jonathanev.review.data.Model.prueba

sealed class AnswerState {
    object Empty : AnswerState()
    data class Filled(val item: QuestionItem) : AnswerState()
}

data class QAItem(
    val question: QuestionItem,
    val answer: AnswerState = AnswerState.Empty,
)
