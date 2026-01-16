package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.data.mapper.xml.toDomain
import com.jonathanev.review.domain.model.GuideDomainModel
import javax.inject.Inject

class GetSaveGuidesUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(): List<GuideDomainModel> {
        return guiaRepository.guidesRecovery
    }
}