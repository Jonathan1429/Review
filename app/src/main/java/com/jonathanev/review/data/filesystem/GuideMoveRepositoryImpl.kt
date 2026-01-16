package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.GuideMoveRepository
import java.io.File
import javax.inject.Inject

class GuideMoveRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider
) : GuideMoveRepository {
    private var guideContext: GuideContext ? = null

    override fun start(guide: GuideContext) {
        this.guideContext = guide
    }

    override fun get(): GuideContext? = guideContext

    override fun clear() {
        guideContext = null
    }

    private fun getGuidePath(sourceGuidePath: String, guideDomain: GuideDomainModel): String {
        return if (guideDomain.version == GuideVersion.V1) {
            filePathsProvider.buildGuide(
                sourceGuidePath,
                guideDomain.nameGuide
            )
        } else {
            filePathsProvider.buildFolderGuide(
                sourceGuidePath,
                guideDomain.nameGuide,
                guideDomain.nameGuide
            )
        }
    }
}