package com.jonathanev.review.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jonathanev.review.R
import com.jonathanev.review.ui.model.QAType
import com.jonathanev.review.ui.preview.ComponentsPreviews
import com.jonathanev.review.ui.preview.providers.QATypeItemProv
import com.jonathanev.review.ui.preview.providers.QATypeItemProvider
import com.jonathanev.review.ui.theme.ComponentTheme
import com.jonathanev.review.ui.theme.ReviewTheme

@ComponentsPreviews
@Composable
fun PreviewQATypeItem(
    @PreviewParameter(QATypeItemProvider::class) data: QATypeItemProv
) {
    ReviewTheme {
        Row {
            QATypeItem(
                qaTypeItem = data.qaTypeItem,
                cardType = data.typeSelected,
                onCardTypeClicked = {}
            )
        }
    }
}

@Composable
fun RowScope.QATypeItem(qaTypeItem: QAType, cardType: QAType, onCardTypeClicked: (QAType) -> Unit) {
    Box(
        modifier = Modifier
            .singleClick(onClick = { onCardTypeClicked(qaTypeItem) })
            .weight(1f)
            .height(56.dp)
            .then(
                if (qaTypeItem == cardType) {
                    Modifier
                        .border(
                            border = BorderStroke(2.dp, ComponentTheme.getSelectedBorderBrush()),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            brush = ComponentTheme.getSelectedBackgroundBrush(),
                            shape = RoundedCornerShape(8.dp)
                        )
                } else {
                    Modifier
                        .border(
                            border = BorderStroke(1.dp, ComponentTheme.getUnselectedBorderColor()),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = ComponentTheme.getUnselectedBackgroundColor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (qaTypeItem == QAType.QUESTION)
                stringResource(R.string.etPregunta)
            else
                stringResource(R.string.etRespuesta),
            color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp
        )
    }
}