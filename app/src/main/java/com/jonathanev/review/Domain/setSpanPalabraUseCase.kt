package com.jonathanev.review.Domain

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import com.jonathanev.review.Data.Model.SpanPalabraModel
import javax.inject.Inject

class setSpanPalabraUseCase @Inject constructor() {
    operator fun invoke(editable: Editable): SpanPalabraModel {
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
            } else if (isColNuevo) {
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

        if (colorSpans.isNotEmpty()) {
            val spansToRemove =
                oldEditable.getSpans(start, endAnterior, ForegroundColorSpan::class.java)

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
        }

        return if (isDoubleColors) {
            SpanPalabraModel(
                editable = editable,
                message = "Sobreescribiste colores y mantuvimos los últimos seleccionados",
                isDoubleColors = true
            )
        } else {
            SpanPalabraModel(
                editable = editable,
                message = "",
            )
        }
    }
}
