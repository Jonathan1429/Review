package com.jonathanev.review.Domain

class TestGetClickRegresarUseCase {
    /*private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val getClickRegresarUseCase = GetClickRegresarUseCase(setPintarTextosUseCase)

    @Test
    fun das_click_en_regresar_y_te_regresa_que_ya_no_tienes_preguntas_anteriores() {
        val contador = 0
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b", "c")

        val result = getClickRegresarUseCase(contador, preguntas, respuestas, "")
        assertEquals(ValidacionesGuiaModel(message = "Ya no tienes preguntas anteriores"), result)
    }

    @Test
    fun das_click_en_regresar_y_te_regresa_la_pregunta_anterior() {
        val contador = 1
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b", "c")
        val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
        val ruta = ""

        every {
            setPintarTextosUseCase.invoke(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns ValidacionesGuiaModel(
            estadoUI = EstadoUI(
                isUpdatedAskAns = true,
                isShowQuitColor = true,
                isShowSelColor = true,
            ),
            builder = SpannableStringBuilder(preguntas[0])
        )

        // Así nos aseguramos que el GetClickRegresarUseCase use el mock
        val getClickRegresarUseCase = GetClickRegresarUseCase(setPintarTextosUseCase)

        val result = getClickRegresarUseCase(contador, preguntas, respuestas, ruta)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                ),
                builder = SpannableStringBuilder(preguntas[0])
            ), result
        )
    }*/
}