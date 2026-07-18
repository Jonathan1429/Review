package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.FileInteractionMode
import com.jonathanev.review.presentation.model.GuideUiModel

data class StudyGuidesProv(
    val listStudyGuides: List<GuideUiModel>,
    val fileInteractionMode: FileInteractionMode
)

class StudyGuidesProvider() : PreviewParameterProvider<StudyGuidesProv> {
    val list = listOf(
        GuideUiModel("Kotlin", "Sintaxis basica de Kotlin"),
        GuideUiModel("Test", "Test unitarios")
    )

    override val values: Sequence<StudyGuidesProv>
        get() = sequenceOf(
            StudyGuidesProv(
                listStudyGuides = list,
                fileInteractionMode = FileInteractionMode.Default
            ),
            StudyGuidesProv(
                listStudyGuides = list,
                fileInteractionMode = FileInteractionMode.MovingItem
            )
        )
}