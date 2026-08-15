package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.ui.model.QAType

data class QASelectTypeProv(
    val typesForSelect: List<QAType>,
    val typeSelected: QAType
)

class QASelectTypeProvider : PreviewParameterProvider<QASelectTypeProv> {
    override val values: Sequence<QASelectTypeProv>
        get() = sequenceOf(
            QASelectTypeProv(
                typesForSelect = listOf(QAType.QUESTION, QAType.ANSWER),
                typeSelected = QAType.QUESTION
            ),
            QASelectTypeProv(
                typesForSelect = listOf(QAType.QUESTION, QAType.ANSWER),
                typeSelected = QAType.ANSWER
            )
        )

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Selected_Question"
            1 -> "Selected_Answer"
            else -> super.getDisplayName(index)
        }
    }
}