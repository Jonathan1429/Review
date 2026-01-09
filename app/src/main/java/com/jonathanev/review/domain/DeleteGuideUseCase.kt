package com.jonathanev.review.domain

import com.jonathanev.review.data.GuiaRepository
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.presentation.event.UIStopEvent
import java.io.File
import javax.inject.Inject

class DeleteGuideUseCase @Inject constructor(
    private val guiaRepository: GuiaRepository,
) {
    operator fun invoke(guideDomainModel: GuideDomainModel, listImages: List<QuestionContentDomain.Image>): UIStopEvent {
        return guiaRepository.deleteGuide(guideDomainModel, listImages)
    }
}