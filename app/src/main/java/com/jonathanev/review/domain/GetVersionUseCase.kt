package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import javax.inject.Inject

class GetVersionUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    operator fun invoke(nameGuide: String) {
        //return guiaRepository.getVersion(nameGuide)
    }
}