package com.jonathanev.review.Domain

import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class setPintarLetraUseCaseTest {
    private lateinit var useCase: setPintarLetraUseCase

    @Before
    fun setUp() {
        useCase = setPintarLetraUseCase()
    }

    @Test
    fun should_apply_color_to_the_last_character() {
        // Arrange
        val editable = SpannableStringBuilder("Hola")
        val cursorPosition = 4
        val color = 0xFF0000 // Color rojo

        val useCase = setPintarLetraUseCase()

        // Act
        useCase.invoke(editable, cursorPosition, color)

        // Assert
        val spans = editable.getSpans(3, 4, ForegroundColorSpan::class.java)
        assert(spans.isNotEmpty()) // Verifica que el span fue aplicado
        assertEquals(color, spans[0].foregroundColor)
    }
}