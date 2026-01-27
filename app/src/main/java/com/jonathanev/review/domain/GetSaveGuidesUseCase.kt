package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuiaRepository
import javax.inject.Inject

class GetSaveGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(): List<GuideDomainModel> {
        return guiaRepository.guidesRecovery
    }
}