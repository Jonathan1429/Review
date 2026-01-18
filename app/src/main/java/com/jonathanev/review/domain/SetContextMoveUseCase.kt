package com.jonathanev.review.domain

import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.repository.GuideMoveRepository
import javax.inject.Inject

class SetContextMoveUseCase @Inject constructor(
    private val guideMoveRepository: GuideMoveRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(guideDomainModel: GuideDomainModel) {
        guideMoveRepository.start(
            GuideContext.Moving(
                guideDomainModel,
                GuidePath(navigationPathRepository.currentPathGuides),
                GuidePath(navigationPathRepository.currentPathImages)
            )
        )
    }
}