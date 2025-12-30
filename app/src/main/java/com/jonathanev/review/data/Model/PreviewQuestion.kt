package com.jonathanev.review.data.Model

import com.jonathanev.review.presentation.model.QuestionContent

data class PreviewQuestion(
    val question: QuestionContent,
    val noImages: String
)