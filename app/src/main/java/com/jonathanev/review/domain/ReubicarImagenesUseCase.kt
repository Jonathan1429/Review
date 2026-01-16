package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.ImagesRepository
import javax.inject.Inject

class ReubicarImagenesUseCase @Inject constructor(
    private val imagesRepository: ImagesRepository
) {
    operator fun invoke(
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>,
        attributesGuide: GuideDomainModel
    ): Boolean {
        return imagesRepository.reubicarImagenes(fileName, preguntas, respuestas, attributesGuide)
    }
}