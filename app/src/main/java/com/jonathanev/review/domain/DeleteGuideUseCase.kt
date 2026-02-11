package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val imagesRepository: ImagesRepository
) {
    operator fun invoke(
        guideDomainModel: GuideDomainModel,
        relativeGuidePath: RelativeGuidePath
    ): DeleteGuideResult {

        return when (val result =
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )) {
            is GetGuideResult.Success -> {
                val tempQuestions =
                    result.list.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }
                        .toList()
                val tempAnswers =
                    result.list.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toList()


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

                return DeleteGuideResult.DeleteSuccess
            }

            else -> DeleteGuideResult.Error
        }
    }
}