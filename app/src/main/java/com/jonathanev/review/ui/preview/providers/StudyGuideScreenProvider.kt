package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.model.ContentType
import com.jonathanev.review.ui.model.QAType

data class StudyGuideScreenProv(
    val typeSelected: QAType,
    val typeForSelected: List<QAType>,
    val mediaSelected: ContentType,
    val mediaForSelected: List<ContentType>,
    val actualQuestion: Int,
    val totalQuestions: Int,
    val listTypeMedia: List<QuestionContentUi.Text>,
    val guideContext: GuideContext,
    val showDialogDeleteQuestion: Boolean,
    val showDialogRepeatGuide: Boolean,
)

private val guideDomainModel = GuideDomainModel(GuideVersion.V2, "", "")
class StudyGuideScreenProvider : PreviewParameterProvider<StudyGuideScreenProv> {
    override val values: Sequence<StudyGuideScreenProv>
        get() = sequenceOf(
            StudyGuideScreenProv(
                typeSelected = QAType.ANSWER,
                typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER),
                mediaSelected = ContentType.IMAGE,
                mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE),
                actualQuestion = 2,
                totalQuestions = 4,
                listTypeMedia = listOf(
                    QuestionContentUi.Text("Primer texto de prueba", emptyList()),
                    QuestionContentUi.Text("Segundo texto de prueba", emptyList())
                ),
                guideContext = GuideContext.Creating(guideDomainModel),
                showDialogDeleteQuestion = false,
                showDialogRepeatGuide = false
            ),
            StudyGuideScreenProv(
                typeSelected = QAType.ANSWER,
                typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER),
                mediaSelected = ContentType.IMAGE,
                mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE),
                actualQuestion = 2,
                totalQuestions = 4,
                listTypeMedia = listOf(
                    QuestionContentUi.Text("Primer texto de prueba", emptyList()),
                    QuestionContentUi.Text("Segundo texto de prueba", emptyList())
                ),
                guideContext = GuideContext.Browsing(guideDomainModel, 0),
                showDialogDeleteQuestion = false,
                showDialogRepeatGuide = false
            ),
            StudyGuideScreenProv(
                typeSelected = QAType.ANSWER,
                typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER),
                mediaSelected = ContentType.IMAGE,
                mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE),
                actualQuestion = 2,
                totalQuestions = 4,
                listTypeMedia = listOf(
                    QuestionContentUi.Text("Primer texto de prueba", emptyList()),
                    QuestionContentUi.Text("Segundo texto de prueba", emptyList())
                ),
                guideContext = GuideContext.Creating(guideDomainModel),
                showDialogDeleteQuestion = true,
                showDialogRepeatGuide = false
            ),
            StudyGuideScreenProv(
                typeSelected = QAType.ANSWER,
                typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER),
                mediaSelected = ContentType.IMAGE,
                mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE),
                actualQuestion = 4,
                totalQuestions = 4,
                listTypeMedia = listOf(
                    QuestionContentUi.Text("Primer texto de prueba", emptyList()),
                    QuestionContentUi.Text("Segundo texto de prueba", emptyList())
                ),
                guideContext = GuideContext.Creating(guideDomainModel),
                showDialogDeleteQuestion = false,
                showDialogRepeatGuide = true
            ),
            StudyGuideScreenProv(
                typeSelected = QAType.QUESTION,
                typeForSelected = listOf(QAType.QUESTION, QAType.ANSWER),
                mediaSelected = ContentType.TEXT,
                mediaForSelected = listOf(ContentType.TEXT, ContentType.IMAGE),
                actualQuestion = 1,
                totalQuestions = 1,
                listTypeMedia = emptyList(), // <--- Lista vacía para probar estado sin elementos
                guideContext = GuideContext.Creating(guideDomainModel),
                showDialogDeleteQuestion = false,
                showDialogRepeatGuide = false
            )
        )

    override fun getDisplayName(index: Int): String {
        return when (index) {
            0 -> "creating_mode"
            1 -> "browsing_mode"
            2 -> "dialog_delete_question"
            3 -> "dialog_repeat_guide"
            4 -> "empty_media_list"
            else -> "item_$index"
        }
    }
}