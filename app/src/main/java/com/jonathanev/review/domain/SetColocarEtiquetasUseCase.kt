package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import javax.inject.Inject

class SetColocarEtiquetasUseCase @Inject constructor() {
    operator fun invoke(
        preguntasProcesadas: List<QuestionItemDomain>,
        respuestasProcesadas: List<QuestionItemDomain>
    ): Pair<List<QuestionItemDomain>, List<QuestionItemDomain>> {
        fun colocarEtiquetas(items: List<QuestionItemDomain>): List<QuestionItemDomain> {
            return items.map { item ->
                val newContent = item.content.map { content ->
                    if (content is QuestionContentDomain.Text) {
                        val sb = StringBuilder(content.text)
                        var offset = 0

                        for (tag in content.colorRangeDomains.sortedBy { it.start }) {
                            val startTag = "«${tag.color}»"
                            val endTag = "«/${tag.color}»"

                            sb.insert(tag.start + offset, startTag)
                            offset += startTag.length
                            sb.insert(tag.end + offset, endTag)
                            offset += endTag.length
                        }

                        val textWithTags = sb.toString()

                        content.copy(
                            text = textWithTags,
                            colorRangeDomains = content.colorRangeDomains
                        )
                    } else {
                        content
                    }
                }
                item.copy(content = newContent)
            }
        }

        val nuevasPreguntas = colocarEtiquetas(preguntasProcesadas)
        val nuevasRespuestas = colocarEtiquetas(respuestasProcesadas)

        return Pair(nuevasPreguntas, nuevasRespuestas)
    }
}