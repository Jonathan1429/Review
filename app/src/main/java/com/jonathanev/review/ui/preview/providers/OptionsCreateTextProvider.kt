package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideMode

data class OptionsCreateTextProv(
    val text: AnnotatedString,
    val color: Color,
    val guideMode: GuideMode
)

class OptionsCreateTextProvider : PreviewParameterProvider<OptionsCreateTextProv> {
    override val values: Sequence<OptionsCreateTextProv>
        get() = sequenceOf(
            OptionsCreateTextProv(
                text = AnnotatedString(""),
                color = Color.Red,
                guideMode = GuideMode.Create("", "")
            ),
            OptionsCreateTextProv(
                text = AnnotatedString("Esto es una prueba de texto"),
                color = Color.Gray,
                guideMode = GuideMode.Create("", "")
            ),
            OptionsCreateTextProv(
                text = AnnotatedString(""),
                color = Color.Blue,
                guideMode = GuideMode.Edit("", "", 0)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString("Esto es una prueba de texto"),
                color = Color.Cyan,
                guideMode = GuideMode.Edit("", "", 0)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString(""),
                color = Color.DarkGray,
                guideMode = GuideMode.Review("", 0)
            ),
            OptionsCreateTextProv(
                text = AnnotatedString("Esto es una prueba de texto"),
                color = Color.Green,
                guideMode = GuideMode.Review("", 0)
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