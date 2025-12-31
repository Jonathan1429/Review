package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.model.QuestionContentDomain
import com.jonathanev.review.presentation.model.QuestionItemDomain
import javax.inject.Inject

class GetContentItemsUseCase @Inject constructor() {
    operator fun invoke(
        contentList: List<QuestionItemDomain>,
        contadorPregunta: Int
    ): Pair<List<QuestionContentDomain.Text>, List<QuestionContentDomain.Image>> {
        val listTexts: MutableList<QuestionContentDomain.Text> = mutableListOf()
        val listImages: MutableList<QuestionContentDomain.Image> = mutableListOf()

        contentList[contadorPregunta].content.forEach { item ->
            when (item) {
                is QuestionContentDomain.Image -> {
                    listImages.add(item.copy())
                }

                is QuestionContentDomain.Text -> {
                    listTexts.add(item.copy())
                }

                QuestionContentDomain.None -> Unit
            }
        }

        return Pair(listTexts, listImages)
    }
}