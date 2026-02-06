package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuideMoveRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import javax.inject.Inject

class SetContextMoveUseCase @Inject constructor(
    private val guideMoveRepository: GuideMoveRepository
) {
    operator fun invoke(guideDomainModel: GuideDomainModel, relativeGuidePath: RelativeGuidePath) {
        guideMoveRepository.start(
            GuideContext.Moving(
                guide = guideDomainModel,
                oldRelativeGuidePath = relativeGuidePath,
                relativeGuidePath = relativeGuidePath
            )
        )

        //navigationPathRepository.reset()
    }
}