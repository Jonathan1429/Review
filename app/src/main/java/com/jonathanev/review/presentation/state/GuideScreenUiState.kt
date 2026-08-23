package com.jonathanev.review.presentation.state

import android.os.Parcelable
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.presentation.model.ColorType
import com.jonathanev.review.presentation.model.QuestionItemUi
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.model.QAType
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface GuideScreenUiState : Parcelable {
    @Parcelize
    data object Loading : GuideScreenUiState

    @Parcelize
    data object Error : GuideScreenUiState

    @Parcelize
    data class Success(
        val fileName: String = "",
        val description: String = "",
        val preguntas: List<QuestionItemUi> = listOf(QuestionItemUi(content = listOf())),
        val respuestas: List<QuestionItemUi> = listOf(QuestionItemUi(content = listOf())),
        val contadorPregunta: Int = 0,
        //val contadorContenido: Int = -1,
        val posContenidoTexto: Int = -1,
        val posContenidoImagen: Int = -1,
        val qAType: QAType = QAType.QUESTION,
        val mediaSelected: ContentType = ContentType.TEXT,
        val guideContext: GuideContext,
        val colorType: ColorType = ColorType.Default,
        val showDialogDeleteQuestion: Boolean = false,
        val showDialogRepeatGuide: Boolean = false,
        val showDialogColor: Boolean = false,
        val showDialogDiscardDraft: Boolean = false,
        val originalQuestions: List<QuestionItemUi>? = null,
        val originalAnswers: List<QuestionItemUi>? = null
    ) : GuideScreenUiState
}