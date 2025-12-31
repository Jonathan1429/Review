package com.jonathanev.review.Domain

import com.jonathanev.review.Domain.repository.FileRepository
import com.jonathanev.review.Domain.model.PreviewQuestionDomain
import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.presentation.state.QAItemDomain
import com.jonathanev.review.presentation.state.ResponseDomain
import javax.inject.Inject

class GetPreviewQuestionsUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(
        domainItems: List<QAItemDomain>
    ): List<PreviewQuestionDomain> {
        val previewQuestionDomain = mutableListOf<PreviewQuestionDomain>()
        val currentPath = fileRepository.getCurrentPath()

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