package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideMode
import com.jonathanev.review.presentation.model.QuestionContentUi
import com.jonathanev.review.ui.screens.toAnnotatedString

data class CreateTextScreenProv(
    val guideMode: GuideMode,
    val textValue: TextFieldValue,
    val showDialog: Boolean
)

class CreateTextScreenProvider : PreviewParameterProvider<CreateTextScreenProv> {
    override val values: Sequence<CreateTextScreenProv>
        get() = sequenceOf(
            CreateTextScreenProv(
                guideMode = GuideMode.Review,
                textValue = TextFieldValue(
                    annotatedString = QuestionContentUi.Text(
                        "Texto de prueba",
                        emptyList()
                    ).toAnnotatedString()
                ),
                showDialog = false
            ),
            CreateTextScreenProv(
                guideMode = GuideMode.Create,
                textValue = TextFieldValue(
                    annotatedString = QuestionContentUi.Text(
                        "Texto de prueba",
                        emptyList()
                    ).toAnnotatedString()
                ),
                showDialog = false,
            ),
            CreateTextScreenProv(
                guideMode = GuideMode.Edit,
                textValue = TextFieldValue(
                    annotatedString = QuestionContentUi.Text(
                        "Texto de prueba",
                        emptyList()
                    ).toAnnotatedString()
                ),
                showDialog = false,
            ),
            CreateTextScreenProv(
                guideMode = GuideMode.Review,
                textValue = TextFieldValue(
                    annotatedString = QuestionContentUi.Text(
                        "",
                        emptyList()
                    ).toAnnotatedString()
                ),
                showDialog = false,
            ),
            CreateTextScreenProv(
                guideMode = GuideMode.Create,
                textValue = TextFieldValue(
                    annotatedString = QuestionContentUi.Text(
                        "",
                        emptyList()
                    ).toAnnotatedString()
                ),
                showDialog = false,
            ),
            CreateTextScreenProv(
                guideMode = GuideMode.Edit,
                textValue = TextFieldValue(
                    annotatedString = QuestionContentUi.Text(
                        "",
                        emptyList()
                    ).toAnnotatedString()
                ),
                showDialog = false,
            ),
            CreateTextScreenProv(
                guideMode = GuideMode.Edit,
                textValue = TextFieldValue(
                    annotatedString = QuestionContentUi.Text(
                        "Texto de prueba",
                        emptyList()
                    ).toAnnotatedString()
                ),
                showDialog = true,
            )
        )

    override fun getDisplayName(index: Int): String? {
        return when (index) {
            0 -> "Review con texto"
            1 -> "Create con texto"
            2 -> "Edit con text"
            3 -> "Review sin texto"
            4 -> "Create sin texto"
            5 -> "Edit sin texto"
            6 -> "Con dialogo"
            else -> super.getDisplayName(index)
        }
    }
}