package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import javax.inject.Inject

class GetImagenesXMLUseCase @Inject constructor() {
    operator fun invoke(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): List<QuestionContentDomain.Image> {
        val allContent = preguntas + respuestas
        val listImagesXML =
            allContent.flatMap { it.content }.filterIsInstance<QuestionContentDomain.Image>()
        return listImagesXML
    }
}