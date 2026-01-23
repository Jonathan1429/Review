package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.model.QuestionContentDomain
import javax.inject.Inject

class SaveGuideImagesUseCase @Inject constructor(
    private val imagesRepository: ImagesRepository
) {
    suspend fun saveImagesInDevice(
        images: List<QuestionContentDomain.Image>,
        guideDomain: GuideDomainModel, ) {
        images.forEach { image ->
            imagesRepository.save(image, guideDomain)
        }
    }
}