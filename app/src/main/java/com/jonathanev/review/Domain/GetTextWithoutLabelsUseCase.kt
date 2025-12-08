package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.prueba.QuestionContent
import javax.inject.Inject

class GetTextWithoutLabelsUseCase @Inject constructor() {
    operator fun invoke(originalText: String): QuestionContent.Text{
        var text = originalText

        while (text.contains("«")) {
            // Eliminar la primera etiqueta y su contenido
            text = text.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            text = text.replaceFirst("«.*?»".toRegex(), "")
        }

        return QuestionContent.Text(text, emptyList())
    }
}