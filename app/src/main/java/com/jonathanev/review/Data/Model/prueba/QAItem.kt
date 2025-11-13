package com.jonathanev.review.Data.Model.prueba

data class QAItem(
    val preguntas: MutableList<QuestionItem>,
    val respuestas: MutableList<QuestionItem>,
)