package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.service.ColorRangeParser
import javax.inject.Inject

class SetPintarTextosUseCase @Inject constructor(
    private val colorRangeParser: ColorRangeParser,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(
        item: QuestionContentDomain
    ): QuestionContentDomain {
        val relativePath = navigationPathRepository.getRelativePath().value

        return when(item){
            is QuestionContentDomain.Image -> {
                QuestionContentDomain.Image(relativePath, item.nameFile)
            }
            is QuestionContentDomain.Text -> {
                colorRangeParser.invoke(item.text)
            }
        }
    }
}