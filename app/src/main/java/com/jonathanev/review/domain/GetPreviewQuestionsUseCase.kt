package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.PreviewQuestionDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.presentation.navigation.NavigationPathRepository
import javax.inject.Inject

class GetPreviewQuestionsUseCase @Inject constructor(
    private val navigationPathRepository: NavigationPathRepository,
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        domainItems: List<QAItemDomain>
    ): MutableList<PreviewQuestionDomain> {
        val previewQuestionDomain = mutableListOf<PreviewQuestionDomain>()
        val currentPath = navigationPathRepository.currentPathGuides

        domainItems.forEach { domainItem ->
            var primerTextoPregunta: QuestionContentDomain = QuestionContentDomain.None
            var totalImgsPregunta = 0

            if (domainItem.question is ResponseDomain.Filled) {
                domainItem.question.item.content.forEach { item ->
                    when (val result = setPintarTextosUseCase.invoke(item, currentPath)) {
                        is QuestionContentDomain.Image -> {
                            totalImgsPregunta++
                        }

                        is QuestionContentDomain.Text -> {
                            if (primerTextoPregunta == QuestionContentDomain.None) {
                                primerTextoPregunta = QuestionContentDomain.Text(
                                    result.text,
                                    result.colorRangeDomains
                                )
                            }
                        }

                        QuestionContentDomain.None -> Unit
                    }
                }
            }

            var totalImgsRespuesta = 0
            if (domainItem.answer is ResponseDomain.Filled) {
                domainItem.answer.item.content.forEach { item ->
                    val result = setPintarTextosUseCase.invoke(item, currentPath)

                    if (result is QuestionContentDomain.Image) {
                        totalImgsRespuesta++
                    }
                }
            }

            previewQuestionDomain.add(
                PreviewQuestionDomain(
                    question = primerTextoPregunta,
                    noImages = (totalImgsPregunta + totalImgsRespuesta).toString()
                )
            )
        }

        return previewQuestionDomain
    }
}