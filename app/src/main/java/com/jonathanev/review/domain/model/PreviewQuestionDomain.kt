package com.jonathanev.review.domain.model

data class PreviewQuestionDomain(
    val question: QuestionContentDomain.Text,
    val noTexts: Int,
    val noImages: Int
)
