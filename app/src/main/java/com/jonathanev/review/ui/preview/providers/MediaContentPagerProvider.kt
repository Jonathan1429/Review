package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.model.ContentType

data class DataMediaContentPagerProvider(
    val listType: List<QuestionContentUi>,
    val sizeList: Int,
    val mediaForSelected: ContentType,
    val guideContext: GuideContext
) {
    override fun toString(): String {
        return "Size_${sizeList}_Type_${mediaForSelected}_Mode_$guideContext"
    }
}

private val guideDomainModel = GuideDomainModel(GuideVersion.V2, "", "")

class MediaContentPagerProvider : PreviewParameterProvider<DataMediaContentPagerProvider> {
    override val values: Sequence<DataMediaContentPagerProvider>
        get() = sequenceOf(
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Text("Hola", emptyList()),
                    QuestionContentUi.Text("Adios", emptyList())
                ),
                sizeList = 2,
                mediaForSelected = ContentType.TEXT,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Image("Uri", "1.png"),
                    QuestionContentUi.Image("Uri", "2.png")
                ),
                sizeList = 2,
                mediaForSelected = ContentType.IMAGE,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Text("Hola", emptyList()),
                ),
                sizeList = 1,
                mediaForSelected = ContentType.TEXT,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
            DataMediaContentPagerProvider(
                listType = emptyList(),
                sizeList = 0,
                mediaForSelected = ContentType.TEXT,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
            DataMediaContentPagerProvider(
                listType = emptyList(),
                sizeList = 0,
                mediaForSelected = ContentType.IMAGE,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Text("Hola", emptyList()),
                    QuestionContentUi.Text("Adios", emptyList())
                ),
                sizeList = 2,
                mediaForSelected = ContentType.TEXT,
                guideContext = GuideContext.Editing(guideDomainModel, 0)
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Text("Hola", emptyList()),
                    QuestionContentUi.Text("Adios", emptyList())
                ),
                sizeList = 2,
                mediaForSelected = ContentType.TEXT,
                guideContext = GuideContext.Browsing(guideDomainModel, 0)
            )
        )
}