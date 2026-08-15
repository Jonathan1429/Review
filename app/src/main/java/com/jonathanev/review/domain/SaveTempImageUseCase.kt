package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.ImagesRepository
import javax.inject.Inject

class SaveTempImageUseCase @Inject constructor(
    private val imagesRepository: ImagesRepository
) {
    suspend operator fun invoke(uri: String): String {
        return imagesRepository.saveTempImage(uri)
    }
}