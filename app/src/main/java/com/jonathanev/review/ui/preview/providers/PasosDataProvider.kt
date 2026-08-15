package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.R


data class PasoPreviewData(
    val image: Int,
    val title: Int,
    val description: Int
)

class PasosDataProvider : PreviewParameterProvider<PasoPreviewData> {
    private val dataList = listOf(
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

    override val values: Sequence<PasoPreviewData> = dataList.asSequence()

    // Sobrescribimos el nombre que Android Studio mostrará en el panel
    override fun getDisplayName(index: Int): String? {
        return when (index) {
            0 -> "Paso1_Crear"
            1 -> "Paso2_Personalizar"
            2 -> "Paso3_Organizar"
            else -> super.getDisplayName(index)
        }
    }
}