package com.jonathanev.review.domain.service

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import javax.inject.Inject

class TextColorRangeGenerator @Inject constructor(
    private val colorRangeParser: ColorRangeParser
) {
    operator fun invoke(tempQuestions: List<QuestionItemDomain>): List<QuestionItemDomain> {
        val resultList = mutableListOf<QuestionItemDomain>()

        tempQuestions.forEach { item ->
            val resultContent = mutableListOf<QuestionContentDomain>()

            item.content.forEach { content ->
                when (content) {
                    is QuestionContentDomain.Text -> {
                        val questionContentDomain = colorRangeParser.invoke(content.text)
                        val modifiedText = content.copy(
                            text = questionContentDomain.text,
                            colorRangeDomains = questionContentDomain.colorRangeDomains
                        )

                        resultContent.add(modifiedText)
                    }
                    else -> {
                        resultContent.add(content)
                    }
                }
            }

            resultList.add(QuestionItemDomain(resultContent.toList()))
        }

        return resultList
    }
}