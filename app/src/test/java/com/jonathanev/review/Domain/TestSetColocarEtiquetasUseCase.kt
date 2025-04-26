package com.jonathanev.review.Domain

import android.text.Editable
import android.text.style.ForegroundColorSpan
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertSame

class TestSetColocarEtiquetasUseCase {
    private var setColocarEtiquetasUseCase = SetColocarEtiquetasUseCase()

    /*@Test
    fun te_regresa_un_editable_sin_colores(){
        val editable = SpannableStringBuilder("x")

        val respuesta = setColocarEtiquetasUseCase.invoke(editable)

        assertEquals(SpannableStringBuilder("x"), respuesta)
    }*/

    @Before
    fun setup() {
        setColocarEtiquetasUseCase = SetColocarEtiquetasUseCase()
        MockKAnnotations.init(this)
    }

    @Test
    fun `invoke sin spans no hace replace y retorna mismo Editable`() {
        // Arrange
        val editable = mockk<Editable>(relaxed = true)
        every { editable.length } returns 5
        every { editable.getSpans(0, 5, ForegroundColorSpan::class.java) } returns arrayOf()

        // Act
        val result = setColocarEtiquetasUseCase.invoke(editable)

        // Assert
        verify(exactly = 0) { editable.replace(any(), any(), any<String>()) }
        assertSame(editable, result, "Debe retornar el mismo Editable instance")
    }

    @Test
    fun `invoke con un span aplica etiquetas correctamente`() {
        // Arrange
        val editable = mockk<Editable>()
        val span = mockk<ForegroundColorSpan>()

        every { editable.length } returns 2
        every { editable.getSpans(0, 2, ForegroundColorSpan::class.java) } returns arrayOf(span)
        every { editable.getSpanStart(span) } returns 0
        every { editable.getSpanEnd(span) } returns 2
        every { span.foregroundColor } returns 123
        every { editable.replace(any(), any(), any<String>()) } returns editable

        // Act
        val result = setColocarEtiquetasUseCase.invoke(editable)

        // Assert
        val opening = "«123»"
        val closing = "«/123»"
        // start = 0, end = 2, opening.length = 5, so closing insertion at 2 + 5 = 7
        verifyOrder {
            editable.replace(0, 0, opening)
            editable.replace(7, 7, closing)
        }
        assertSame(editable, result)
    }
}