package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val imagesRepository: ImagesRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(
        guideDomainModel: GuideDomainModel
    ): DeleteGuideResult {
        val relativeGuidePath = navigationPathRepository.relativePath.first()

        return when (val result =
            guiaRepository.getXMLGuide(guideDomainModel)) {
            is GetGuideResult.Success -> {
                val tempQuestions =
                    result.list.map { it.question }.toList()
                val tempAnswers =
                    result.list.map { it.answer }.toList()


                val listImages = (tempQuestions + tempAnswers).flatMap { it.content }
                    .filterIsInstance<QuestionContentDomain.Image>()

                val deleteGuide =
                    guiaRepository.deleteGuide(
                        deleteGuide = GuideContext.DeleteGuide(guideDomainModel, relativeGuidePath)
                    )
                if (!deleteGuide) {
                    return DeleteGuideResult.ErrorGuide
                }
                val deleteImages =
                    imagesRepository.deleteImages(guideDomainModel, listImages, relativeGuidePath)
                if (!deleteImages) {
                    return DeleteGuideResult.ErrorImage
                }

                DeleteGuideResult.DeleteSuccess
            }

            else -> DeleteGuideResult.Error
        }
    }
}