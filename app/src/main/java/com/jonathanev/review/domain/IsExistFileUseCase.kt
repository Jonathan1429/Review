package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import javax.inject.Inject

class IsExistFileUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository
) {
    suspend operator fun invoke(name: String): Boolean {
        return guiaRepository.existGuide(name)
    }
}