package com.jonathanev.review.presentation.model

data class PreviewQuestionUi(
    val id: String,
    val question: QuestionContentUi.Text,
    val noTexts: String,
    val noImages: String
)