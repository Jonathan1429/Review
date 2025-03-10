package com.jonathanev.review.Domain

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import javax.inject.Inject

class setPintarLetraUseCase @Inject constructor() {
    operator fun invoke(texto: Editable?, cursorPosition: Int, colorActual: Int) {
        texto?.let { letra ->
            if (letra.isNotEmpty()) {
                val lastCharIndex = cursorPosition - 1

                // Aplicar color a la letra actual
                letra.setSpan(
                    ForegroundColorSpan(colorActual),
                    lastCharIndex,
                    lastCharIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            val allSpans = letra.getSpans(0, letra.length, ForegroundColorSpan::class.java)
            Log.d("Spans", "Spans after update: ${letra.getSpans(0, letra.length, ForegroundColorSpan::class.java).size}")

            /*texto?.let {
                if (it.isNotEmpty() && !pregResBandera) {
                    val cursorPosition = binding!!.etPregResp.selectionStart
                    val lastCharIndex = cursorPosition - 1
                    posColorFinal = lastCharIndex + 1

                    it.setSpan(
                        ForegroundColorSpan(colorActual),
                        lastCharIndex,
                        lastCharIndex + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    binding!!.etPregResp.setSelection(lastCharIndex + 1)
                    pregResBandera = false
                }
            }*/
        }
    }
}