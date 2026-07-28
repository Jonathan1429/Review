package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuideMoveRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideMoveRepositoryImpl @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) : GuideMoveRepository {
    private var guideContext: GuideContext.Moving? = null

    override suspend fun start(guideDomainModel: GuideDomainModel) {
        val relativePath = navigationPathRepository.getRelativePath()

        guideContext = GuideContext.Moving(
            guide = guideDomainModel,
            oldRelativeGuidePath = relativePath
        )
    }

    override fun get(): GuideContext.Moving? = guideContext

    override fun clear() {
        guideContext = null
    }
}