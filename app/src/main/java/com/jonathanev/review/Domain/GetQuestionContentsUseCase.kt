package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import javax.inject.Inject

class GetQuestionContentsUseCase @Inject constructor() {
    operator fun invoke(
        questions: MutableList<QuestionItem>,
        contadorPregunta: Int
    ): List<QuestionContent> {
        return questions[contadorPregunta].content
    }
}