package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.presentation.model.GuideMode

data class CreateImageContentProv(
    val guideMode: GuideMode,
    val uriImage: String
)

class CreateImageContentProvider : PreviewParameterProvider<CreateImageContentProv> {
    override val values: Sequence<CreateImageContentProv>
        get() = sequenceOf(
            CreateImageContentProv(
                guideMode = GuideMode.Edit,
                uriImage = "path/uri_cargada.png",
            ),
            CreateImageContentProv(
                guideMode = GuideMode.Create,
                uriImage = "",
            ),
            CreateImageContentProv(
                guideMode = GuideMode.Review,
                uriImage = "",
            ),
            CreateImageContentProv(
                guideMode = GuideMode.Review,
                uriImage = "path/uri_cargada.png",
            ),
        )

    override fun getDisplayName(index: Int): String? {
        return when (index) {
            0 -> "Edit con Uri"
            1 -> "Create sin Uri"
            2 -> "Review sin Uri"
            3 -> "Review con Uri"
            else -> super.getDisplayName(index)
        }
    }
}