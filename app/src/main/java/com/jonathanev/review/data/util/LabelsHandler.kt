package com.jonathanev.review.data.util

import com.jonathanev.review.data.model.xml.ColorRangeXmlDto
import com.jonathanev.review.data.model.xml.QuestionContentXmlDto
import javax.inject.Inject

class LabelsHandler @Inject constructor() {

    private val tagRegex = Regex("""«(/)?([^»]+)»""")

    /**
     * Sanitiza el texto eliminando únicamente las etiquetas cuyo flujo de
     * apertura y cierre no sea estrictamente secuencial y plano.
     */
    fun sanitizeLabels(input: String): String {
        if (input.isEmpty()) return input

        val invalidTagIds = mutableSetOf<String>()
        var currentlyOpenTag: String? = null

        val matches = tagRegex.findAll(input)

        for (match in matches) {
            val isClosing = match.groupValues[1] == "/"
            val tagId = match.groupValues[2]

            if (!isClosing) {
                // Si ya había una etiqueta abierta y llega otra apertura, la primera fue violada
                if (currentlyOpenTag != null) {
                    invalidTagIds.add(currentlyOpenTag)
                }
                currentlyOpenTag = tagId
            } else {
                if (currentlyOpenTag == tagId) {
                    // Cierre correcto de la etiqueta actual
                    currentlyOpenTag = null
                } else {
                    // Cierre sin apertura previa o desalineado con la etiqueta activa
                    currentlyOpenTag?.let { invalidTagIds.add(it) }
                    invalidTagIds.add(tagId)
                    currentlyOpenTag = null
                }
            }
        }

        // Si el texto termina con una etiqueta sin cerrar
        currentlyOpenTag?.let { invalidTagIds.add(it) }

        if (invalidTagIds.isEmpty()) return input

        // Elimina solo las etiquetas (apertura y cierre) de los IDs marcados como inválidos
        return tagRegex.replace(input) { match ->
            val tagId = match.groupValues[2]
            if (tagId in invalidTagIds) "" else match.value
        }
    }

    fun processAndSanitizeLabels(originalText: String): QuestionContentXmlDto.Text {
        val colorRanges = mutableListOf<ColorRangeXmlDto>()
        var text = originalText

        while (text.contains("«")) {
            val startTag = text.indexOf("«")
            val endTag = text.indexOf("»")
            if (endTag == -1 || startTag >= endTag) break

            val colorString = text.substring(startTag + 1, endTag)
            val colorInt = colorString.toIntOrNull() ?: 0
            val delTags = colorString.length + 2
            val startText = endTag + 1 - delTags

            text = text.replaceFirst("«.*?»".toRegex(), "")

            var endText = text.indexOf("«", startText)
            if (endText == -1) {
                endText = text.length
            }

            text = text.replaceFirst("«.*?»".toRegex(), "")

            colorRanges.add(
                ColorRangeXmlDto(
                    start = startText,
                    end = endText,
                    color = colorInt
                )
            )
        }

        return QuestionContentXmlDto.Text(
            text = text,
            colorRangeXmlDto = colorRanges
        )
    }
}