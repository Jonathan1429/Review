package com.jonathanev.review.UI.Utils

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan

object SpanCleaner {
    fun cleanOverlappingSpans(editable: Editable) {
        val colorSpans: Array<ForegroundColorSpan> =
            editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)
        val oldEditable = editable
        val sortedSpans = colorSpans.sortedBy { editable.getSpanStart(it) }

        var start = -1
        var end = 0
        var endAnterior = 0
        var isColNuevo = false
        var colorAnterior = 0
        var colorNuevo = 0
        var isDoubleColors = false
        var toCleaningColors = false

        for (colorSpan: ForegroundColorSpan in sortedSpans) {
            if (start == -1) {
                start = editable.getSpanStart(colorSpan)
            }

            if (end > editable.getSpanStart(colorSpan)) {
                isDoubleColors = true
                toCleaningColors = true
            }

            end = editable.getSpanEnd(colorSpan)
            colorNuevo = colorSpan.foregroundColor

            if (colorAnterior != colorNuevo) {
                if (colorAnterior == 0) {
                    isColNuevo = false
                    colorAnterior = colorNuevo
                    endAnterior = end - 1
                } else {
                    isColNuevo = true
                }
            }

            //if ((end - endAnterior) != 1) {
            // Limpiar colores encimados
            if (toCleaningColors) {
                // Obtener los spans dentro del rango especificado
                val spansToRemove = oldEditable.getSpans(
                    start,
                    endAnterior,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    if (span.foregroundColor == colorAnterior) {
                        editable.removeSpan(span)
                    }
                }

                start = editable.getSpanStart(colorSpan)
                endAnterior = end

                colorAnterior = colorNuevo
                isColNuevo = false
                toCleaningColors = false
            } else if (isColNuevo || (end - endAnterior) != 1) {
                // Obtener los spans dentro del rango especificado
                val spansToRemove = oldEditable.getSpans(
                    start,
                    endAnterior,
                    ForegroundColorSpan::class.java
                )

                for (span in spansToRemove) {
                    if (span.foregroundColor == colorAnterior) {
                        editable.removeSpan(span)
                    }
                }

                editable.setSpan(
                    ForegroundColorSpan(colorAnterior),
                    start,
                    endAnterior,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                start = editable.getSpanStart(colorSpan)
                endAnterior = end

                colorAnterior = colorNuevo
                isColNuevo = false
                // toCleaningColors = false
            } else {
                endAnterior = end
            }
        }

    }

    /*private fun removeColor(editable: Editable, endAnterior: Int, start: Int, end: Int, color: Int) {
            val spansToRemove =
                editable.getSpans(start, endAnterior, ForegroundColorSpan::class.java)

            for (span in spansToRemove) {
                if (span.foregroundColor == colorAnterior) {
                    editable.removeSpan(span)
                }
            }

            editable.setSpan(
                ForegroundColorSpan(colorAnterior),
                start,
                endAnterior,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

        val spansToRemove =
            editable.getSpans(start, end, ForegroundColorSpan::class.java)

        spansToRemove.forEach {
            if (it.foregroundColor == color) editable.removeSpan(it)
        }

        editable.setSpan(
            ForegroundColorSpan(color),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }*/
}