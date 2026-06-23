package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.R

data class PasoPreviewData(
    val image: Int,
    val title: Int,
    val description: Int
)

class PasosDataProvider : PreviewParameterProvider<PasoPreviewData> {
    override val values: Sequence<PasoPreviewData> = sequenceOf(
        PasoPreviewData(
            image = R.drawable.ic_plus_circle_outline,
            title = R.string.lblTitleStepOne,
            description = R.string.lblDescStepOne
        ),
        PasoPreviewData(
            image = R.drawable.ic_palette,
            title = R.string.lblTitleStepTwo,
            description = R.string.lblDescStepTwo
        ),
        PasoPreviewData(
            image = R.drawable.ic_folder_move_outline,
            title = R.string.lblTitleStepThree,
            description = R.string.lblDescStepThree
        )
    )
}