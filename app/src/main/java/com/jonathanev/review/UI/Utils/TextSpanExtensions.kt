package com.jonathanev.review.UI.Utils

import android.text.Editable
import android.text.Spannable
import android.text.style.ForegroundColorSpan

fun Editable.paintLetter(cursorPosition: Int, color: Int) {
    setSpan(
        ForegroundColorSpan(color),
        cursorPosition - 1,
        cursorPosition,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}