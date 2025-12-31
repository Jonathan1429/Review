package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.model.QuestionContentDomain
import javax.inject.Inject

class GetTextWithoutLabelsUseCase @Inject constructor() {
    operator fun invoke(originalText: String): QuestionContentDomain.Text{
        var text = originalText

        while (text.contains("«")) {
            // Eliminar la primera etiqueta y su contenido
            text = text.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            text = text.replaceFirst("«.*?»".toRegex(), "")
        }

        return QuestionContentDomain.Text(text, emptyList())
    }
}