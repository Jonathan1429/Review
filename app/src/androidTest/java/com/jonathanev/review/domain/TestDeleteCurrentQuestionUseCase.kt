package com.jonathanev.review.domain

class TestDeleteCurrentQuestionUseCase {
    /*private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val deleteCurrentQuestionUseCase = DeleteCurrentQuestionUseCase(setPintarTextosUseCase)

    @Test
    fun te_va_a_regresar_una_pregunta_anterior() {
        val preguntas = arrayListOf("a", "b")
        val respuestas = arrayListOf("a", "b")
        val contador = 3
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
                isThereMoreAsks = true
            ),
            builder = SpannableStringBuilder("b"),
        )

        val result = deleteCurrentQuestionUseCase(preguntas, respuestas, contador, ruta)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("b")
            ), result
        )
        // preguntas tenía "a", "b", y debe quedar solo "a"
        assertEquals(listOf("a", "b"), preguntas)

        // respuestas tenía solo "a", y debe mantenerse igual
        assertEquals(listOf("a", "b"), respuestas)
    }

    @Test
    fun te_debe_regresar_a_y_debe_de_eliminar_la_b() {
        val preguntas = arrayListOf("a", "b")
        val respuestas = arrayListOf("a")
        val contador = 1
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
                isThereMoreAsks = true
            ),
            builder = SpannableStringBuilder("a"),
        )

        val result = deleteCurrentQuestionUseCase(preguntas, respuestas, contador, ruta)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("a")
            ), result
        )
        // preguntas tenía "a", "b", y debe quedar solo "a"
        assertEquals(listOf("a"), preguntas)

        // respuestas tenía solo "a", y debe mantenerse igual
        assertEquals(listOf("a"), respuestas)
    }

    @Test
    fun al_estar_en_la_primer_pregunta_no_regresa_nada() {
        val preguntas = arrayListOf("a", "b")
        val respuestas = arrayListOf("a", "b")
        val contador = 0
        val ruta = ""

        val result = deleteCurrentQuestionUseCase(preguntas, respuestas, contador, ruta)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true
                )
            ), result
        )
    }

    @Test
    fun no_se_elimina_nada_ya_que_no_hay_datos_que_eliminar() {
        val preguntas = arrayListOf<String>()
        val respuestas = arrayListOf<String>()
        val contador = 0
        val ruta = ""

        val result = deleteCurrentQuestionUseCase(preguntas, respuestas, contador, ruta)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true
                )
            ), result
        )
        // preguntas tenía "a", "b", y debe quedar solo "a"
        assertEquals(emptyList<String>(), preguntas)

        // respuestas tenía solo "a", y debe mantenerse igual
        assertEquals(emptyList<String>(), respuestas)
    }

    @Test
    fun este_test_no_deberia_ejecutarse_porque_no_es_posible_contador_menor_a_0() {
        // Este test solo se aplicó para tener el 100% del coverage
        val preguntas = arrayListOf("a", "b")
        val respuestas = arrayListOf("a", "b")
        val contador = -1
        val ruta = ""

        val result = deleteCurrentQuestionUseCase(preguntas, respuestas, contador, ruta)

        // Nada debe cambiar
        assertEquals(listOf("a", "b"), preguntas)
        assertEquals(listOf("a", "b"), respuestas)

        // Y debe devolverte el modelo de isClearText porque entra al if contadorPregunta <= 0
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true
                )
            ),
            result
        )
    }*/
}