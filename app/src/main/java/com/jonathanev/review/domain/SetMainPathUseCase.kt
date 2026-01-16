package com.jonathanev.review.domain

import com.jonathanev.review.data.repository.NavigationPathRepository
import javax.inject.Inject

class SetMainPathUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke() {
        navigationPathRepository.reset()
    }
}
