package com.jonathanev.review.presentation.model

data class EstadoUI(
    val textList: List<QuestionContentUi.Text> = emptyList(),
    val imageList: List<QuestionContentUi.Image> = emptyList(),
)