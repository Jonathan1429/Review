package com.jonathanev.review.presentation.state

import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.model.QAType

sealed interface GuideScreenUiState {
    data object Loading : GuideScreenUiState
    data object Error : GuideScreenUiState

    data class Success(
        val fileName: String = "",
        val description: String = "",
        val preguntas: List<QuestionItemUi> = listOf(QuestionItemUi(content = listOf())),
        val respuestas: List<QuestionItemUi> = listOf(QuestionItemUi(content = listOf())),
        val contadorPregunta: Int = 0,
        val contadorContenido: Int = 0,
        val qAType: QAType = QAType.QUESTION,
        val mediaSelected: ContentType = ContentType.TEXT,
        val isEditing: Boolean = false,
        val isLastQuestion: Boolean? = false,
        val showDialogDeleteQuestion: Boolean = false,
        val showDialogRepeatGuide: Boolean = false
    ) : GuideScreenUiState
}