package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.service.ColorRangeParser
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val colorRangeParser: ColorRangeParser,
) {
    operator fun invoke(
        item: QuestionContentDomain
    ): QuestionContentDomain {
        return when(item){
            is QuestionContentDomain.Image -> {
                QuestionContentDomain.Image(item.uri, item.nameFile)
            }
            is QuestionContentDomain.Text -> {
                colorRangeParser.invoke(item.text)
            }
        }
    }
}