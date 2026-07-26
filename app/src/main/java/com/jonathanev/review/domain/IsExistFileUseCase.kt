package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IsExistFileUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(name: String): Boolean {
        val relativeGuidePath = navigationPathRepository.relativePath.first()
        return guiaRepository.existGuide(name, relativeGuidePath)
    }
}