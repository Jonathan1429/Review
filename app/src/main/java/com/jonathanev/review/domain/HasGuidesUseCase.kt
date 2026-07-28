package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import javax.inject.Inject

class HasGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
) {
    suspend operator fun invoke(): Boolean {
        return guiaRepository.hasGuides()
    }
}