package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.model.QuestionContent
import com.jonathanev.review.presentation.model.QuestionItem
import javax.inject.Inject

class GetContentItemsUseCase @Inject constructor() {
    operator fun invoke(
        contentList: List<QuestionItem>,
        contadorPregunta: Int
    ): Pair<List<QuestionContent.Text>, List<QuestionContent.Image>> {
        val listTexts: MutableList<QuestionContent.Text> = mutableListOf()
        val listImages: MutableList<QuestionContent.Image> = mutableListOf()

        contentList[contadorPregunta].content.forEach { item ->
            when (item) {
                is QuestionContent.Image -> {
                    listImages.add(item.copy())
                }

                is QuestionContent.Text -> {
                    listTexts.add(item.copy())
                }

                QuestionContent.None -> Unit
            }
        }

        return Pair(listTexts, listImages)
    }
}