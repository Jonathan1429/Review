package com.jonathanev.review.ui.preview.providers

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion

data class CreateImageContentProv(
    val guideContext: GuideContext,
    val uriImage: String
)

val guideDomain = GuideDomainModel(GuideVersion.V2, "Test", "")

class CreateImageContentProvider : PreviewParameterProvider<CreateImageContentProv> {
    override val values: Sequence<CreateImageContentProv>
        get() = sequenceOf(
            CreateImageContentProv(
                guideContext = GuideContext.Editing(guideDomain, 0),
                uriImage = "path/uri_cargada.png",
            ),
            CreateImageContentProv(
                guideContext = GuideContext.Creating(guideDomain),
                uriImage = "",
            ),
            CreateImageContentProv(
                guideContext = GuideContext.Browsing(guideDomain, 0),
                uriImage = "",
            ),
            CreateImageContentProv(
                guideContext = GuideContext.Browsing(guideDomain, 0),
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