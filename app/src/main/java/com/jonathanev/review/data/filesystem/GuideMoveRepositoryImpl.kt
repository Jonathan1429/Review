package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideMoveRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideMoveRepositoryImpl @Inject constructor() : GuideMoveRepository {
    private var guideContext: GuideContext.Moving? = null

    override fun start(guide: GuideContext.Moving) {
        this.guideContext = guide
    }

    override fun get(): GuideContext.Moving? = guideContext

    override fun clear() {
        guideContext = null
    }
}