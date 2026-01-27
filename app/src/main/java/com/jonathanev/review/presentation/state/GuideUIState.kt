package com.jonathanev.review.presentation.state

import com.jonathanev.review.domain.model.QAType
import com.jonathanev.review.presentation.model.QuestionItemUi

data class GuideUiState(
    val preguntas: List<QuestionItemUi> = emptyList(),
    val respuestas: List<QuestionItemUi> = emptyList(),
    val contadorPregunta: Int = 0,
    val contadorContenido: Int = -1,
    val qAType: QAType = QAType.QUESTION,
    val fileName: String = "",
    val isEditing: Boolean = false,
    val actualUri: String? = null,
    val isLastQuestion: Boolean? = null
)