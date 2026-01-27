package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.NavigationPathRepository
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
                GuidePath(navigationPathRepository.getPathGuides().value),
                GuidePath(navigationPathRepository.getPathImages().value)
            )
        )

        navigationPathRepository.reset()
    }
}