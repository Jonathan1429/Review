package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.ui.model.QAType

data class GuideUiState(
    val preguntas: List<QuestionItemUi> = emptyList(),
    val respuestas: List<QuestionItemUi> = emptyList(),
    val contadorPregunta: Int = 0,
    val contadorContenido: Int = 0,
    val qAType: QAType = QAType.QUESTION,
    val fileName: String = "",
    val description: String = "",
    val isEditing: Boolean = false,
    val actualUri: String? = null,
    val isLastQuestion: Boolean? = false
)