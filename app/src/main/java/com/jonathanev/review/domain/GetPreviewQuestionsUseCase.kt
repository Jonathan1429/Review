package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.PreviewQuestionDomain
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPreviewQuestionsUseCase @Inject constructor(
    private val setPintarTextosUseCase: SetPintarTextosUseCase,
    private val navigationPathRepository: NavigationPathRepository
) {
    suspend operator fun invoke(domainItems: List<QAItemDomain>): List<PreviewQuestionDomain> {
        val relativeGuidePath = navigationPathRepository.relativePath.first()

        val previewQuestionDomain = mutableListOf<PreviewQuestionDomain>()

        domainItems.forEach { domainItem ->
            var primerTextoPregunta: QuestionContentDomain.Text? = null
            var totalImgsPregunta = 0
            var totalTextsPregunta = 0

            domainItem.question.content.forEach { item ->
                when (val result =
                    setPintarTextosUseCase.invoke(item, relativeGuidePath.value)) {
                    is QuestionContentDomain.Image -> {
                        totalImgsPregunta++
                    }

                    is QuestionContentDomain.Text -> {
                        totalTextsPregunta++

                        if (primerTextoPregunta == null) {
                            primerTextoPregunta = QuestionContentDomain.Text(
                                result.text,
                                result.colorRangeDomains
                            )
                        }
                    }
                }
            }

            var totalImgsRespuesta = 0
            var totalTextsRespuesta = 0

            domainItem.answer.content.forEach { item ->
                if (item is QuestionContentDomain.Image) {
                    totalImgsRespuesta++
                }
                if (item is QuestionContentDomain.Text) {
                    totalTextsRespuesta++
                }
            }

            previewQuestionDomain.add(
                PreviewQuestionDomain(
                    question = primerTextoPregunta ?: QuestionContentDomain.Text(
                        "No se encuentra texto a cargar",
                        emptyList()
                    ),
                    noTexts = totalTextsPregunta + totalTextsRespuesta,
                    noImages = totalImgsPregunta + totalImgsRespuesta
                )
            )
        }

        return previewQuestionDomain
    }
}