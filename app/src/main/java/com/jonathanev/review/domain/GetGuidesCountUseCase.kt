package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import javax.inject.Inject

class GetGuidesCountUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(): Int {
        return guiaRepository.getNumGuides()
    }
}