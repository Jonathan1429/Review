package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuideMoveRepository
import javax.inject.Inject

class ClearGuideMoveUseCase @Inject constructor(
    private val guideMoveRepository: GuideMoveRepository
) {
    operator fun invoke() {
        guideMoveRepository.clear()
    }
}