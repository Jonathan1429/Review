package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.result.DeleteGuideResult
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
    private val imagesRepository: ImagesRepository,
    private val navigationPathRepository: NavigationPathRepository
) {
    operator fun invoke(
        guideDomainModel: GuideDomainModel,
        listImages: List<QuestionContentDomain.Image>
    ): DeleteGuideResult {
        val deleteGuide = guiaRepository.deleteGuide(
            guideDomainModel, GuideContext.Browsing(
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

        return DeleteGuideResult.DeleteSuccess
    }
}