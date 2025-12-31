package com.jonathanev.review.Domain.model

import com.jonathanev.review.presentation.model.QuestionContentDomain

data class PreviewQuestionDomain(
    val question: QuestionContentDomain,
    val noImages: String
)
