package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.prueba.ColorRange
import com.jonathanev.review.Data.Model.prueba.QuestionContent
import javax.inject.Inject

class GetColorRanges @Inject constructor() {
    operator fun invoke(originalText: String): QuestionContent.Text{
        val colorRange: List<ColorRange> = mutableListOf()
        var text = originalText
        var contColorPreg: Int = 0

        while (text.contains("«")) {
            var startTag = text.indexOf("«")
            val endTag = text.indexOf("»")
            val color: Int = text.substring(startTag + 1, endTag).toInt()
            //val longColor: Int = color.length
            //val colEntero: Int = color.toInt()
            startTag = endTag//fin + 1
            val startText = endTag + 1
            val endText = text.indexOf("«", startText)

            colorRange.plus(ColorRange(startText, endText, color))

            // Eliminar la primera etiqueta y su contenido
            text = text.replaceFirst("«.*?»".toRegex(), "")

            // Eliminar la segunda etiqueta y su contenido
            text = text.replaceFirst("«.*?»".toRegex(), "")
            contColorPreg++
        }

        return QuestionContent.Text(text, colorRange)
    }
}