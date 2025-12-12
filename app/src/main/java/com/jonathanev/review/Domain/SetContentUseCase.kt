package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.ContentWrapper
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import javax.inject.Inject

class SetContentUseCase @Inject constructor() {
    operator fun invoke(
        newContent: QuestionContent,
        mutablePivRefList: MutableList<QuestionItem>,
        contadorPregunta: Int,
        contadorContenido: Int,
        isEditingMode: Boolean,
        filterType: Class<out QuestionContent>
    ) {
        val originalItem = mutablePivRefList[contadorPregunta]
        val originalContent = originalItem.content.toMutableList()

        // Envolver con índice real
        val wrappers = originalItem.content.mapIndexed { index, content ->
            ContentWrapper(index, content)
        }

        // Filtrar por Tipo de Contenido
        val filtered = wrappers.filter { filterType.isInstance(it.content) }

        if (isEditingMode) {
            val realIndex = filtered[contadorContenido].originalIndex
            originalContent[realIndex] = newContent
        } else {
            originalContent.add(newContent)
        }

        mutablePivRefList[contadorPregunta] = originalItem.copy(content = originalContent.toList())
    }
}