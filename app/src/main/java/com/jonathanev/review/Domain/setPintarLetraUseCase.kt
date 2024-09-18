package com.jonathanev.review.Domain

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import javax.inject.Inject

class setPintarLetraUseCase @Inject constructor() {
    operator fun invoke(texto: Editable?, cursorPosition: Int, colorActual: Int) {
        texto?.let { letra ->
            if (letra.isNotEmpty()) {
                val colorSpans: Array<ForegroundColorSpan> =
                    texto.getSpans(0, texto.length, ForegroundColorSpan::class.java)
                val sortedSpans = colorSpans.sortedBy { texto.getSpanStart(it) }

                val lastCharIndex = cursorPosition - 1

                letra.setSpan(
                    ForegroundColorSpan(colorActual),
                    lastCharIndex,
                    lastCharIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}