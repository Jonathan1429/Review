package com.jonathanev.review.domain

import com.jonathanev.review.data.repository.NavigationPathRepository
import javax.inject.Inject

class BackPathUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(){
        navigationPathRepository.back()
    }
}