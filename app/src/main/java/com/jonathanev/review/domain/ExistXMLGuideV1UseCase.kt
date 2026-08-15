package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.ExistGuideV1Result
import javax.inject.Inject

class ExistXMLGuideV1UseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    suspend operator fun invoke(guideDomainModel: GuideDomainModel): ExistGuideV1Result {
        return guiaRepository.existXMLGuideV1(guideDomainModel)
    }
}