package com.jonathanev.review.domain

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import javax.inject.Inject

class SetPintarLetraUseCase @Inject constructor() {
    operator fun invoke(letter: Editable, cursorPosition: Int, actualColor: Int) {
        // Aplicar color a la letra actual
        letter.setSpan(
            ForegroundColorSpan(actualColor),
            cursorPosition - 1,    // Painting color - start
            cursorPosition,        // Painting color - end
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}