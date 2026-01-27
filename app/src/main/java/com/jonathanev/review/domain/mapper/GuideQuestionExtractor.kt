package com.jonathanev.review.domain.mapper

import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.service.TextColorRangeGenerator
import javax.inject.Inject

class GuideQuestionExtractor @Inject constructor(
    private val textColorRangeGenerator: TextColorRangeGenerator
) {
    fun map(result: GetGuideResult.Success): Pair<List<QuestionItemDomain>, List<QuestionItemDomain>> {
        val questions =
            result.list.mapNotNull { (it.question as? ResponseDomain.Filled)?.item }
        val answers =
            result.list.mapNotNull { (it.answer as? ResponseDomain.Filled)?.item }

        return Pair(textColorRangeGenerator.invoke(questions), textColorRangeGenerator.invoke(answers))
    }
}