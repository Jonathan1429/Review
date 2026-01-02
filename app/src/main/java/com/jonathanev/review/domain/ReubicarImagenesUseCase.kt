package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.ImagesRepository
import javax.inject.Inject

class ReubicarImagenesUseCase @Inject constructor(
    private val getVersionUseCase: GetVersionUseCase,
    private val imagesRepository: ImagesRepository
) {
    operator fun invoke(
        fileName: String,
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ) {
        val version = getVersionUseCase.invoke()
        imagesRepository.reubicarImagenes(version, fileName, preguntas, respuestas)
    }
}