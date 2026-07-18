package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.model.ContentType

data class DataMediaContentPagerProvider(
    val listType: List<QuestionContentUi>,
    val sizeList: Int,
    val mediaForSelected: ContentType,
    val guideMode: GuideMode
) {
    override fun toString(): String {
        return "Size: $sizeList - Type: $mediaForSelected - Mode: $guideMode"
    }
}

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
                guideMode = GuideMode.Create("", "")
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Image("Uri", "1.png"),
                    QuestionContentUi.Image("Uri", "2.png")
                ),
                sizeList = 2,
                mediaForSelected = ContentType.IMAGE,
                guideMode = GuideMode.Create("", "")
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Text("Hola", emptyList()),
                ),
                sizeList = 1,
                mediaForSelected = ContentType.TEXT,
                guideMode = GuideMode.Create("", "")
            ),
            DataMediaContentPagerProvider(
                listType = emptyList(),
                sizeList = 0,
                mediaForSelected = ContentType.TEXT,
                guideMode = GuideMode.Create("", "")
            ),
            DataMediaContentPagerProvider(
                listType = emptyList(),
                sizeList = 0,
                mediaForSelected = ContentType.IMAGE,
                guideMode = GuideMode.Create("", "")
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Text("Hola", emptyList()),
                    QuestionContentUi.Text("Adios", emptyList())
                ),
                sizeList = 2,
                mediaForSelected = ContentType.TEXT,
                guideMode = GuideMode.Edit("", "", 0)
            ),
            DataMediaContentPagerProvider(
                listType = listOf(
                    QuestionContentUi.Text("Hola", emptyList()),
                    QuestionContentUi.Text("Adios", emptyList())
                ),
                sizeList = 2,
                mediaForSelected = ContentType.TEXT,
                guideMode = GuideMode.Review("", 0)
            )
        )
}