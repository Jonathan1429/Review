package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SetDecodePathImageUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend operator fun invoke(
        preguntas: List<QuestionItemDomain>,
        respuestas: List<QuestionItemDomain>
    ): Pair<List<QuestionItemDomain>, List<QuestionItemDomain>> { // Retorna ambas listas actualizadas

        var count = userPreferencesRepository.getCountImage().first()

        // Función interna que devuelve una NUEVA lista, no modifica la anterior
        fun transformItems(items: List<QuestionItemDomain>): List<QuestionItemDomain> {
            return items.map { item ->
                val newContent = item.content.map { content ->
                    if (content is QuestionContentDomain.Image && content.nameFile.isEmpty()) {
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

        userPreferencesRepository.setImageCount(count)

        return Pair(nuevasPreguntas, nuevasRespuestas)
    }
}