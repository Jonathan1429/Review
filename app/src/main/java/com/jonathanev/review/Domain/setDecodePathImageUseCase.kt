package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import com.jonathanev.review.Data.Model.prueba.QuestionItem
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetDecodePathImageUseCase @Inject constructor(
    private val dataStore: DataStoreManager
) {

    suspend operator fun invoke(
        preguntas: MutableList<QuestionItem>,
        respuestas: MutableList<QuestionItem>
    ) {
        var count = dataStore.getCountImage().first()

        fun updateItems(items: MutableList<QuestionItem>) {
            items.forEachIndexed { indexItem, item ->
                var wasUpdated = false

                val newContent = item.content.map { content ->
                    if (content is QuestionContent.Image) {
                        wasUpdated = true

                        val updated = if (content.nameFile.isEmpty()) {
                            val newContent = content.copy(
                                nameFile = "$count.png"
                            )
                            count++
                            newContent
                        } else {
                            val nameFile = content.uri.substringAfterLast("/")
                            content.copy(
                                nameFile = nameFile
                            )
                        }
                        updated
                    } else {
                        content
                    }
                }

                if (wasUpdated) {
                    items[indexItem] = item.copy(content = newContent)
                }
            }
        }

        updateItems(preguntas)
        updateItems(respuestas)

        dataStore.setCounter(count)
    }
}