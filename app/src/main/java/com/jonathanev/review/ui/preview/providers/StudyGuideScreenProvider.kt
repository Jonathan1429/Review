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
            )
        )
}