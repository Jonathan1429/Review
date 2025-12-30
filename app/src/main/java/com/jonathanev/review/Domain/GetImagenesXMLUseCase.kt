package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.data.Model.prueba.QuestionItem
import javax.inject.Inject

class GetImagenesXMLUseCase @Inject constructor() {
    operator fun invoke(
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>
    ): List<QuestionContent.Image> {
        val allContent = preguntas + respuestas
        val listImagesXML =
            allContent.flatMap { it.content }.filterIsInstance<QuestionContent.Image>()
        return listImagesXML
    }
}