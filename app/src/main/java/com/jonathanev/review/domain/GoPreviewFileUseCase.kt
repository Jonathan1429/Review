package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePathContext
import com.jonathanev.review.domain.model.GuidePathTarget
import com.jonathanev.review.domain.repository.PathProvider
import javax.inject.Inject

class GoPreviewFileUseCase @Inject constructor(
    private val pathProvider: PathProvider
) {
    operator fun invoke(cachedGuides: List<GuideDomainModel>, nameGuide: String) {
        /*val guide = cachedGuides.find { it.nameGuide == nameGuide } ?: return
        val context = GuidePathContext(
            version = guide.version,
            guideName = guide.nameGuide,
            target = GuidePathTarget.GUIDE_FILE,
        )
        pathProvider.resolveGuidePath(context)*/
    }
}