package com.jonathanev.review.domain.mapper

import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.service.TextColorRangeGenerator
import javax.inject.Inject

class GuideQuestionExtractor @Inject constructor(
    private val textColorRangeGenerator: TextColorRangeGenerator
) {
    fun map(result: GetGuideResult.Success): Pair<List<QuestionItemDomain>, List<QuestionItemDomain>> {
        val questions =
            result.list.map { (it.question ) }.toList()
        val answers =
            result.list.map { (it.answer ) }.toList()

        return Pair(textColorRangeGenerator.invoke(questions), textColorRangeGenerator.invoke(answers))
    }
}