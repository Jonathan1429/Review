package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.model.QuestionContentDomain
import java.io.File
import javax.inject.Inject

class SaveGuideImagesUseCase @Inject constructor(
    private val imagesRepository: ImagesRepository
) {
    suspend fun saveImagesInDevice(images: List<QuestionContentDomain.Image>, imagesPath: File) {
        images.forEach { image ->
            imagesRepository.saveImage(image, imagesPath)
        }
    }
}