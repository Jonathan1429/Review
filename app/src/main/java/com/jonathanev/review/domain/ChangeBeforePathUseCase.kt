package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.PathProvider
import javax.inject.Inject

class ChangeBeforePathUseCase @Inject constructor(
    private val pathProvider: PathProvider,
) {
    operator fun invoke(){
        pathProvider.setBeforePath()
    }
}