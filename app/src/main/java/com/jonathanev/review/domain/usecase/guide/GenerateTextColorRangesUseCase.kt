package com.jonathanev.review.domain.usecase.guide

import com.jonathanev.review.domain.GetColorRangesUseCase
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import javax.inject.Inject

class GenerateTextColorRangesUseCase @Inject constructor(
    private val getColorRangesUseCase: GetColorRangesUseCase
) {
    operator fun invoke(tempQuestions: List<QuestionItemDomain>): List<QuestionItemDomain> {
        val resultList = mutableListOf<QuestionItemDomain>()
        val resultContent = mutableListOf<QuestionContentDomain>()

        tempQuestions.forEach { item ->
            resultContent.clear()

            item.content.forEach { content ->
                when (content) {
                    is QuestionContentDomain.Text -> {
                        val questionContentDomain = getColorRangesUseCase.invoke(content.text)
                        val modifiedText = content.copy(
                            text = questionContentDomain.text,
                            colorRangeDomains = questionContentDomain.colorRangeDomains
                        )

                        resultContent.add(modifiedText)
                    }
                    is QuestionContentDomain.Image -> {
                        resultContent.add(content)
                    }
                    QuestionContentDomain.None -> {
                        resultContent.add(content)
                    }
                }
            }

            resultList.add(QuestionItemDomain(resultContent.toList()))
        }

        return resultList
    }
}