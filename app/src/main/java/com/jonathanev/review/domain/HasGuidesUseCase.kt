package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HasGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
) {
    operator fun invoke(): Flow<Boolean> {
        return guiaRepository.hasGuides()
    }
}