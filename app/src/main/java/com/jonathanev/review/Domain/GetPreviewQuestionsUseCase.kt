package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.PreviewQuestion
import com.jonathanev.review.data.Model.prueba.AnswerState
import com.jonathanev.review.data.Model.prueba.QAItem
import com.jonathanev.review.data.Model.prueba.QuestionContent
import com.jonathanev.review.Domain.repository.FileRepository
import javax.inject.Inject

class GetPreviewQuestionsUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val setPintarTextosUseCase: SetPintarTextosUseCase
) {
    operator fun invoke(qaItems: List<QAItem>): MutableList<PreviewQuestion> {
        val previewQuestion = mutableListOf<PreviewQuestion>()
        val currentPath = fileRepository.getCurrentPath()

        qaItems.forEach { qa ->

            // ----------------------------
            //   PREGUNTA
            // ----------------------------
            var primerTextoPregunta: QuestionContent = QuestionContent.None
            var totalImgsPregunta = 0

            qa.question.content.forEach { item ->
                when (val result = setPintarTextosUseCase.invoke(item, currentPath)) {
                    is QuestionContent.Image -> {
                        totalImgsPregunta++
                    }

                    is QuestionContent.Text -> {
                        if (primerTextoPregunta == QuestionContent.None) {
                            primerTextoPregunta = QuestionContent.Text(
                                result.text,
                                result.colorRanges
                            )
                        }
                    }

                    QuestionContent.None -> Unit
                }
            }

            // ----------------------------
            //   RESPUESTA
            // ----------------------------
            var totalImgsRespuesta = 0

            if (qa.answer is AnswerState.Filled) {
                qa.answer.item.content.forEach { item ->
                    val result = setPintarTextosUseCase.invoke(item, currentPath)

                    if (result is QuestionContent.Image) {
                        totalImgsRespuesta++
                    }
                }
            }

            previewQuestion.add(
                PreviewQuestion(
                    question = primerTextoPregunta,
                    noImages = (totalImgsPregunta + totalImgsRespuesta).toString()
                )
            )
        }

        return previewQuestion
    }
}