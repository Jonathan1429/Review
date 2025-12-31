package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionItemDomain
import javax.inject.Inject

class GetImagenesXMLUseCase @Inject constructor() {
    operator fun invoke(
        preguntas: MutableList<QuestionItemDomain>,
        respuestas: MutableList<QuestionItemDomain>
    ): List<QuestionContentDomain.Image> {
        val allContent = preguntas + respuestas
        val listImagesXML =
            allContent.flatMap { it.content }.filterIsInstance<QuestionContentDomain.Image>()
        return listImagesXML
    }
}