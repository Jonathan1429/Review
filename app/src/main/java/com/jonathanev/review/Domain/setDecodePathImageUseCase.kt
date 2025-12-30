package com.jonathanev.review.Domain

import com.jonathanev.review.data.Model.DataStoreManager
import com.jonathanev.review.presentation.model.QuestionContent
import com.jonathanev.review.presentation.model.QuestionItem
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetDecodePathImageUseCase @Inject constructor(
    private val dataStore: DataStoreManager
) {
    suspend operator fun invoke(
        preguntas: List<QuestionItem>,
        respuestas: List<QuestionItem>
    ): Pair<List<QuestionItem>, List<QuestionItem>> { // Retorna ambas listas actualizadas

        var count = dataStore.getCountImage().first()

        // Función interna que devuelve una NUEVA lista, no modifica la anterior
        fun transformItems(items: List<QuestionItem>): List<QuestionItem> {
            return items.map { item ->
                val newContent = item.content.map { content ->
                    if (content is QuestionContent.Image && content.nameFile.isEmpty()) {
                        count++
                        content.copy(nameFile = "$count.png")
                    } else {
                        content
                    }
                }
                item.copy(content = newContent)
            }
        }

        val nuevasPreguntas = transformItems(preguntas)
        val nuevasRespuestas = transformItems(respuestas)

        dataStore.setCounter(count)

        return Pair(nuevasPreguntas, nuevasRespuestas)
    }
}