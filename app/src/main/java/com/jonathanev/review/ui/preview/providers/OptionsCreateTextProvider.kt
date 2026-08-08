package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion

data class OptionsCreateTextProv(
    val text: AnnotatedString,
    val color: Color,
    val guideContext: GuideContext
)

private val guideDomainModel = GuideDomainModel(GuideVersion.V2, "", "")

class OptionsCreateTextProvider : PreviewParameterProvider<OptionsCreateTextProv> {
    override val values: Sequence<OptionsCreateTextProv>
        get() = sequenceOf(
            OptionsCreateTextProv(
                text = AnnotatedString(""),
                color = Color.Red,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString("Esto es una prueba de texto"),
                color = Color.Gray,
                guideContext = GuideContext.Creating(guideDomainModel)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString(""),
                color = Color.Blue,
                guideContext = GuideContext.Editing(guideDomainModel, 0)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString("Esto es una prueba de texto"),
                color = Color.Cyan,
                guideContext = GuideContext.Editing(guideDomainModel, 0)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString(""),
                color = Color.DarkGray,
                guideContext = GuideContext.Browsing(guideDomainModel, 0)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString("Esto es una prueba de texto"),
                color = Color.Green,
                guideContext = GuideContext.Browsing(guideDomainModel, 0)
            )
        )

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Create_sin_texto"
            1 -> "Create_con_texto"
            2 -> "Edit_sin_texto"
            3 -> "Edit_con_texto"
            4 -> "Review_sin_texto"
            5 -> "Review_con_texto"
            else -> super.getDisplayName(index)
        }
    }
}