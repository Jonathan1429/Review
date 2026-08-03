package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuiaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(): Flow<List<GuideDomainModel>> {
        return guiaRepository.getGuides()
    }
}