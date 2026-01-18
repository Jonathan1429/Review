package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.service.TextColorRangeGenerator
import javax.inject.Inject

class UploadContentUseCase @Inject constructor(
    private val textColorRangeGenerator: TextColorRangeGenerator
) {
    operator fun invoke(result: GetGuideResult.Success): Pair<List<QuestionItemDomain>, List<QuestionItemDomain>> {
        val tempQuestions =
            result.list.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }
                .toList()
        val tempAnswers =
            result.list.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }.toList()

        val questionsDomain = textColorRangeGenerator.invoke(tempQuestions)
        val answersDomain = textColorRangeGenerator.invoke(tempAnswers)
        return Pair(questionsDomain, answersDomain)
    }
}