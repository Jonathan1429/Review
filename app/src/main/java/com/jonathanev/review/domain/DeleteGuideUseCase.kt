package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.PathResolve
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.service.TextColorRangeGenerator
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val imagesRepository: ImagesRepository,
    private val navigationPathRepository: NavigationPathRepository,
    private val getObtenerDatosXMLUseCase: GetObtenerDatosXMLUseCase,
    private val pathResolve: PathResolve,
    private val textColorRangeGenerator: TextColorRangeGenerator
) {
    operator fun invoke(
        guideDomainModel: GuideDomainModel
    ): DeleteGuideResult {
        val context = GuideContext.Actual(
            guide = guideDomainModel,
            currentGuidePath = pathResolve.currentPathResolve(guideDomainModel)
        )

        return when (val result = getObtenerDatosXMLUseCase.invoke(context)) {
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

                val deleteGuide = guiaRepository.deleteGuide(
                    guideDomainModel, GuideContext.DeleteGuide(
                        GuidePath(navigationPathRepository.currentPathGuides)
                    )
                )
                if (!deleteGuide) {
                    return DeleteGuideResult.ErrorGuide
                }
                val deleteImages = imagesRepository.delete(guideDomainModel, listImages)
                if (!deleteImages) {
                    return DeleteGuideResult.ErrorImage
                }

                navigationPathRepository.reset()
                return DeleteGuideResult.DeleteSuccess
            }

            GetGuideResult.Error -> DeleteGuideResult.Error

            GetGuideResult.InvalidFormat -> DeleteGuideResult.InvalidFormat

            GetGuideResult.NotFound -> DeleteGuideResult.NotFound

            GetGuideResult.UnknownError -> DeleteGuideResult.UnknownError
        }
    }
}