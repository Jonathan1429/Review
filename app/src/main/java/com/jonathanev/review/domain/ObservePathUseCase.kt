package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePathUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(): Flow<RelativeGuidePath> {
        return navigationPathRepository.getRelativePathFlow()
    }
}