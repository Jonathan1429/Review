package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.repository.GuideContextRepository
import javax.inject.Inject

class SetContextEditUseCase @Inject constructor(
    private val guideContextRepository: GuideContextRepository
) {
    suspend operator fun invoke(guideContextEdit: GuideContext.Editing) {
        return guideContextRepository.start(guideContextEdit)
    }
}