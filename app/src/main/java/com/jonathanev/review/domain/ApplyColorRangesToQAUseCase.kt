package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import javax.inject.Inject

class ApplyColorRangesToQAUseCase @Inject constructor(
    private val generateTextColorRangesUseCase: GenerateTextColorRangesUseCase
) {
    operator fun invoke(items: List<QAItemDomain>): List<QAItemDomain> {
        return items.map { qa ->
            qa.copy(
                question = qa.question.process(),
                answer = qa.answer.process()
            )
        }
    }

    private fun ResponseDomain.process(): ResponseDomain =
        when (this) {
            is ResponseDomain.Filled -> {
                val updatedItem =
                    generateTextColorRangesUseCase.invoke(listOf(item)).first()
                copy(item = updatedItem)
            }
            else -> this
        }
}
