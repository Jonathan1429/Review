package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val imagesRepository: ImagesRepository
) {
    suspend operator fun invoke(
        guideDomainModel: GuideDomainModel
    ): DeleteGuideResult {
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
                        deleteGuide = GuideContext.DeleteGuide(guideDomainModel)
                    )
                if (!deleteGuide) {
                    return DeleteGuideResult.ErrorGuide
                }
                val deleteImages =
                    imagesRepository.deleteImages(guideDomainModel, listImages)
                if (!deleteImages) {
                    return DeleteGuideResult.ErrorImage
                }

                DeleteGuideResult.DeleteSuccess
            }

            else -> DeleteGuideResult.Error
        }
    }
}