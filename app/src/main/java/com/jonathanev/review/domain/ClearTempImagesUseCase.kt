package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.ImagesRepository
import javax.inject.Inject

class ClearTempImagesUseCase @Inject constructor(
    private val imagesRepository: ImagesRepository
) {
    suspend operator fun invoke() {
        imagesRepository.clearTempImages()
    }
}