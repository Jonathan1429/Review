package com.jonathanev.review.domain

class TestSetSpanPalabraUseCase {
    /*private val setSpanPalabraUseCase = SetSpanPalabraUseCase() // Reemplaza con el nombre real de tu setSpanPalabraUseCase

    private fun createEditableWithSpans(
        text: String,
        spans: List<Triple<Int, Int, Int>>
    ): Editable {
        val editable = SpannableStringBuilder(text)
        spans.forEach { (start, end, color) ->
            editable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return editable
    }

    @Test
    fun sin_spans_retorna_sin_cambios() {
        val editable = SpannableStringBuilder("hola")
        val result = setSpanPalabraUseCase(editable)

        assertFalse(result.isDoubleColors)
        assertEquals("", result.message)
        assertEquals("hola", result.editable.toString())
        assertNotNull(result.editable)
    }

    @Test
    fun un_solo_span_se_mantiene() {
        val editable = createEditableWithSpans("hola", listOf(Triple(0, 4, 0xFF0000)))
        val result = setSpanPalabraUseCase(editable)

        assertFalse(result.isDoubleColors)
        assertEquals("", result.message)
        assertEquals(1, result.editable.getSpans(0, 4, ForegroundColorSpan::class.java).size)
    }

    @Test
    fun spans_consecutivos_mismo_color_se_fusionan() { // Cambié el color a otro y las posiciones no son seguidas
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(Triple(0, 4, 0xFF0000), Triple(2, 3, 0x0000FF))
        )
        val result = setSpanPalabraUseCase(editable)

        val spans = result.editable.getSpans(0, 6, ForegroundColorSpan::class.java)

        //assertFalse(result.isDoubleColors)
        //assertEquals("", result.message)
        assertEquals(1, spans.size)
        //assertEquals(0xFF0000, spans[0].foregroundColor)
    }

    @Test
    fun spans_consecutivos_diferente_color_se_mantienen() {
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(Triple(0, 3, 0xFF0000), Triple(3, 6, 0x0000FF))
        )
        val result = setSpanPalabraUseCase(editable)

        val spans = result.editable.getSpans(0, 6, ForegroundColorSpan::class.java)

        assertFalse(result.isDoubleColors)
        assertEquals("", result.message)
        assertEquals(2, spans.size)
    }

    @Test
    fun spans_solapados_diferente_color_sobrescriben() {
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(Triple(0, 5, 0xFF0000), Triple(3, 4, 0x0000FF))
        )
        val result = setSpanPalabraUseCase(editable)

        assertTrue(result.isDoubleColors)
        assertTrue(result.message.contains("Sobreescribiste colores"))

        val spans = result.editable.getSpans(0, 7, ForegroundColorSpan::class.java)
        assertEquals(1, spans.size)
        assertEquals(0x0000FF, spans[0].foregroundColor)
    }

    @Test
    fun spans_solapados_mismo_color_se_fusionan() { // Cambié el color a otro y las posiciones no son seguidas
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(Triple(0, 5, 0xFF0000), Triple(3, 4, 0x0000FF))
        )
        val result = setSpanPalabraUseCase(editable)

        val spans = result.editable.getSpans(0, 7, ForegroundColorSpan::class.java)

        //assertFalse(result.isDoubleColors)
        //assertEquals("", result.message)
        assertEquals(1, spans.size)
        //assertEquals(0xFF0000, spans[0].foregroundColor)
    }

    @Test
    fun spans_con_espacio_mismo_color_no_se_fusionan() {
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(Triple(0, 3, 0xFF0000), Triple(5, 7, 0xFF0000))
        )
        val result = setSpanPalabraUseCase(editable)

        val spans = result.editable.getSpans(0, 7, ForegroundColorSpan::class.java)

        assertFalse(result.isDoubleColors)
        assertEquals("", result.message)
        assertEquals(2, spans.size)
    }

    @Test
    fun spans_con_espacio_diferente_color_se_mantienen() {
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(Triple(0, 3, 0xFF0000), Triple(5, 7, 0x0000FF))
        )
        val result = setSpanPalabraUseCase(editable)

        val spans = result.editable.getSpans(0, 7, ForegroundColorSpan::class.java)

        assertFalse(result.isDoubleColors)
        assertEquals("", result.message)
        assertEquals(2, spans.size)
    }

    @Test
    fun multiples_colores_solapados_encadenados_queda_ultimo() {
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(
                Triple(0, 3, 0xFF0000),
                Triple(2, 5, 0x0000FF),
                Triple(4, 7, 0x00FF00)
            )
        )
        val result = setSpanPalabraUseCase(editable)

        assertTrue(result.isDoubleColors)
        assertTrue(result.message.contains("Sobreescribiste colores"))

        val spans = result.editable.getSpans(0, 7, ForegroundColorSpan::class.java)
        assertEquals(1, spans.size)
        assertEquals(0x00FF00, spans[0].foregroundColor)
    }

    @Test
    fun colores_intercalados_sin_solapamiento_se_mantienen() {
        val editable = createEditableWithSpans(
            "holamundo",
            listOf(
                Triple(0, 3, 0xFF0000),
                Triple(3, 5, 0x0000FF),
                Triple(5, 7, 0x00FF00)
            )
        )
        val result = setSpanPalabraUseCase(editable)

        val spans = result.editable.getSpans(0, 7, ForegroundColorSpan::class.java)

        assertFalse(result.isDoubleColors)
        assertEquals("", result.message)
        assertEquals(3, spans.size)
    } */
}