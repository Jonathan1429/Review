package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.GuideMoveRepository
import javax.inject.Inject

class SetContextMoveUseCase @Inject constructor(
    private val guideMoveRepository: GuideMoveRepository
) {
    suspend operator fun invoke(guideDomainModel: GuideDomainModel) {
        guideMoveRepository.start(guideDomainModel)
    }
}