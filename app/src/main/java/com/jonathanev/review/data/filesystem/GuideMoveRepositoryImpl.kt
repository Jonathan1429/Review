package com.jonathanev.review.data.filesystem

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuideMoveRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideMoveRepositoryImpl @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) : GuideMoveRepository {
    private val _guideContext = MutableStateFlow<GuideContext.Moving?>(null)
    override val guideContext: StateFlow<GuideContext.Moving?> = _guideContext.asStateFlow()

    override suspend fun start(guideDomainModel: GuideDomainModel) {
        val relativePath = navigationPathRepository.getRelativePath()

        _guideContext.value = GuideContext.Moving(
            guide = guideDomainModel,
            oldRelativeGuidePath = relativePath
        )
    }

    override fun clear() {
        _guideContext.value = null
    }
}