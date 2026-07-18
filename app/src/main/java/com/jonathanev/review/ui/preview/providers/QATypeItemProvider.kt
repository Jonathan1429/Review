package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.ui.model.QAType

data class QATypeItemProv(
    val qaTypeItem: QAType,
    val typeSelected: QAType
)

class QATypeItemProvider: PreviewParameterProvider<QATypeItemProv> {
    override val values: Sequence<QATypeItemProv>
        get() = sequenceOf(
            QATypeItemProv(
                qaTypeItem = QAType.QUESTION,
                typeSelected = QAType.QUESTION
            ),
            QATypeItemProv(
                qaTypeItem = QAType.QUESTION,
                typeSelected = QAType.ANSWER
            ),
            QATypeItemProv(
                qaTypeItem = QAType.ANSWER,
                typeSelected = QAType.ANSWER
            ),
            QATypeItemProv(
                qaTypeItem = QAType.ANSWER,
                typeSelected = QAType.QUESTION
            )
        )

    override fun getDisplayName(index: Int): String? {
        return when(index) {
            0 -> "Question selected"
            1 -> "Question no selected"
            2 -> "Answer selected"
            3 -> "Answer no selected"
            else -> super.getDisplayName(index)
        }
    }
}