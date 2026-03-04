package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.ExistGuideV1Result
import javax.inject.Inject

class ExistXMLGuideV1UseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(guideDomainModel: GuideDomainModel, relativeGuidePath: RelativeGuidePath): ExistGuideV1Result {
        return guiaRepository.existXMLGuideV1(guideDomainModel, relativeGuidePath)
    }
}