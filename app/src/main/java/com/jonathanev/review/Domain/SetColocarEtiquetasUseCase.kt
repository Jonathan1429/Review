package com.jonathanev.review.Domain

import android.text.Editable
import android.text.style.ForegroundColorSpan
import javax.inject.Inject

class SetColocarEtiquetasUseCase @Inject constructor() {
    operator fun invoke(editable: Editable): Editable {
        val colorSpans: Array<ForegroundColorSpan> = editable.getSpans(
            0,
            editable.length,
            ForegroundColorSpan::class.java
        )

        for (colorSpan: ForegroundColorSpan in colorSpans) {
            val start: Int = editable.getSpanStart(colorSpan)
            val end: Int = editable.getSpanEnd(colorSpan)
            val color: Int = colorSpan.foregroundColor

            val etiqIni: String = "«$color»"
            val etiqFin: String = "«/$color»"

            // Agregar la etiqueta de inicio al texto
            editable.replace(start, start, etiqIni)

            // Agregar la etiqueta de cierre al texto
            editable.replace(end + etiqIni.length, end + etiqIni.length, etiqFin)
        }

        return editable
    }
}