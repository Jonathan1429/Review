package com.jonathanev.review.ui.components

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import com.google.android.material.textfield.TextInputEditText

class RichColorEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.editTextStyle
) : TextInputEditText(context, attrs, defStyleAttr) {

    var colorActual: Int = Color.WHITE

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val target = super.onCreateInputConnection(outAttrs) ?: return null

        // Envolvemos la conexión original en nuestro propio Wrapper modificado
        return object : InputConnectionWrapper(target, true) {

            override fun setComposingText(text: CharSequence?, newCursorPosition: Int): Boolean {
                return super.setComposingText(aplicarColorAlTextoEntrante(text), newCursorPosition)
            }

            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                return super.commitText(aplicarColorAlTextoEntrante(text), newCursorPosition)
            }
        }
    }

    private fun aplicarColorAlTextoEntrante(text: CharSequence?): CharSequence? {
        if (text == null || text.isEmpty() || colorActual == Color.WHITE) {
            return text
        }

        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(colorActual),
            0,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }
}