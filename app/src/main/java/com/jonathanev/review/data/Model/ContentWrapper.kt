package com.jonathanev.review.data.Model

import com.jonathanev.review.presentation.model.QuestionContent

data class ContentWrapper(
    val originalIndex: Int,
    val content: QuestionContent
)