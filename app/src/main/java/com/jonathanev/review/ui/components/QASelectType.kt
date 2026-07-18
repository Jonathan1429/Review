package com.jonathanev.review.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.jonathanev.review.ui.model.QAType
import com.jonathanev.review.ui.preview.DevicePreviews
import com.jonathanev.review.ui.preview.providers.QASelectTypeProv
import com.jonathanev.review.ui.preview.providers.QASelectTypeProvider

@DevicePreviews
@Composable
fun PreviewQASelectType(
    @PreviewParameter(QASelectTypeProvider::class) data: QASelectTypeProv
) {
    QASelectType(
        typeForSelected = data.typesForSelect,
        typeSelected = data.typeSelected,
        onTypeClicked = {}
    )
}

@Composable
fun QASelectType(
    typeForSelected: List<QAType>,
    typeSelected: QAType,
    onTypeClicked: (QAType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        typeForSelected.forEach { item ->
            QATypeItem(
                item, typeSelected,
                onTypeClicked = { typeClicked ->
                    onTypeClicked(typeClicked)
                }
            )
        }
    }
}