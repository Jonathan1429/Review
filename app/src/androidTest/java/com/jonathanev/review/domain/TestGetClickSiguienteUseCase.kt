package com.jonathanev.review.domain

class TestGetClickSiguienteUseCase {
    /*private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val getClickSiguienteUseCase = GetClickSiguienteUseCase(setPintarTextosUseCase)

    @Test
    fun das_click_siguiente_sino_hay_mas_te_regresa_un_mensaje_que_no_hay_mas_preguntas() {
        val contadorPregunta = 2
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b", "c")
        val ruta = ""

        val resultado = getClickSiguienteUseCase(contadorPregunta, preguntas, respuestas, ruta)
        assertEquals(
            ValidacionesGuiaModel(
                message = "Se acabaron las preguntas, ¿Quieres repetir la guia?",
            ), resultado
        )
    }

    @Test
    fun das_click_siguiente_si_hay_mas_preguntas_te_va_a_regresar_esa() {
        val contadorPregunta = 1 // Pregunta actual = b
        val preguntas = arrayListOf("a", "c", "e")
        val respuestas = arrayListOf("b", "d", "f")
        val ruta = ""

        // Si nosotros ponemos los valores tenemos que
        every {
            setPintarTextosUseCase.invoke(
                isEtPregunta = true,
                question = preguntas,
                answer = respuestas,
                contadorPregunta = contadorPregunta + 1, // Pregunta que pintará = c
                ruta = ruta
            )
        } returns ValidacionesGuiaModel(
            estadoUI = EstadoUI(
                isUpdatedAskAns = true,
                isShowQuitColor = true,
                isShowSelColor = true,
            ),
            builder = SpannableStringBuilder(preguntas[2]),
            //builder = SpannableStringBuilder("c")
        )

        // Si ponemos hacemos que el mock ponga los valores necesarios para que nos regrese ese reultado
        /*every {
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
            builder = SpannableStringBuilder(preguntas[2]),
            //builder = SpannableStringBuilder("c")
        )*/

        val resultado = getClickSiguienteUseCase(contadorPregunta, preguntas, respuestas, ruta)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                ),
                builder = SpannableStringBuilder(preguntas[2]),
            ), resultado
        )
    }*/
}