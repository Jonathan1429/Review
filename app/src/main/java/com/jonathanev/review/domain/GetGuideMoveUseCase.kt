package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideMoveRepository
import javax.inject.Inject

class GetGuideMoveUseCase @Inject constructor(
    private val guideMoveRepository: GuideMoveRepository
) {
    operator fun invoke(): GuideContext? {
        return guideMoveRepository.get()
    }
}