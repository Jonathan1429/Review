package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import javax.inject.Inject

class GetColorRangesUseCase @Inject constructor() {
    operator fun invoke(originalText: String): QuestionContentDomain.Text{
        val colorRangeDomain = mutableListOf<ColorRangeDomain>()
        var text = originalText
        var contColorPreg = 0

        while (text.contains("«")) {
            val startTag = text.indexOf("«")
            val endTag = text.indexOf("»")
            val color: Int = text.substring(startTag + 1, endTag).toInt()
            val delTags: Int = ( color.toString().length + 2 )
            val startText = endTag + 1 - delTags

            // Eliminar la primera etiqueta y su contenido
            text = text.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            val endText = text.indexOf("«", startText )
            text = text.replaceFirst("«.*?»".toRegex(), "")

            colorRangeDomain.add(ColorRangeDomain(startText, endText, color))

            contColorPreg++
        }

        return QuestionContentDomain.Text(text, colorRangeDomain.toList())
    }
}