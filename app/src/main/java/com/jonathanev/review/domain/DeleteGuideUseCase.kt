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
import com.jonathanev.review.domain.service.TextColorRangeGenerator
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val imagesRepository: ImagesRepository,
    private val textColorRangeGenerator: TextColorRangeGenerator
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

                val questionsDomain = textColorRangeGenerator.invoke(tempQuestions)
                val answersDomain = textColorRangeGenerator.invoke(tempAnswers)

                val listImages = (questionsDomain + answersDomain).flatMap { it.content }
                    .filterIsInstance<QuestionContentDomain.Image>()

                val deleteGuide =
                    guiaRepository.deleteGuide(
                        deleteGuide = GuideContext.DeleteGuide(guideDomainModel, relativeGuidePath)
                    )
                if (!deleteGuide) {
                    return DeleteGuideResult.ErrorGuide
                }
                val deleteImages = imagesRepository.deleteImages(guideDomainModel, listImages, relativeGuidePath)
                if (!deleteImages) {
                    return DeleteGuideResult.ErrorImage
                }

                //navigationPathRepository.reset()
                return DeleteGuideResult.DeleteSuccess
            }

            GetGuideResult.Error -> DeleteGuideResult.Error

            GetGuideResult.InvalidFormat -> DeleteGuideResult.InvalidFormat

            GetGuideResult.NotFound -> DeleteGuideResult.NotFound

            GetGuideResult.UnknownError -> DeleteGuideResult.UnknownError
        }
    }
}