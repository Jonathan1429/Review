package com.jonathanev.review.Domain

import com.jonathanev.review.presentation.model.ColorRange
import javax.inject.Inject

class SetColocarEtiquetasUseCase @Inject constructor() {
    operator fun invoke(text: String, listSpans: List<ColorRange>): String {
        val sb = StringBuilder(text)
        var offset = 0

        for (tag in listSpans.sortedBy { it.start }) {
            val startTag = "«${tag.color}»"
            val endTag = "«/${tag.color}»"

            sb.insert(tag.start + offset, startTag)
            offset += startTag.length
            sb.insert(tag.end + offset, endTag)
            offset += endTag.length
        }

        return sb.toString()
    }
}