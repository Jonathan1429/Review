package com.jonathanev.review.Domain

class TestGetObtenerDatosXMLUseCase {
    /*//private val guiaProvider
    private val guiaRepository = mockk<GuiaRepository>()
    private val getObtenerDatosXMLUseCase = GetObtenerDatosXMLUseCase(guiaRepository)

    @Test
    fun `Obtener los datos del xml del archivo mandado`() {
        val nombreArchivo = "A.xml"
        val ruta = "storage/"
        val preguntasRespuestasEsperadas = listOf(
            QuestionAnswerModel("¿Qué es Kotlin?", "Un lenguaje de programación"),
            QuestionAnswerModel("¿Qué es Android?", "Un sistema operativo")
        )

        every {
            guiaRepository.obtenerDatosXMLV1(
                nombreArchivo,
            )
        } returns preguntasRespuestasEsperadas

        val resultado = getObtenerDatosXMLUseCase(nombreArchivo)

        // Assert
        assertEquals(preguntasRespuestasEsperadas, resultado)
    }

    @Test
    fun `Sino se encuentra la guia solicitada te regresa una lista vacia`() {
        val nombreArchivo = "A.xml"
        val ruta = "storage/"

        every {
            guiaRepository.obtenerDatosXMLV1(
                nombreArchivo,
            )
        } returns emptyList()

        val resultado = getObtenerDatosXMLUseCase(nombreArchivo)

        // Assert
        assertTrue(resultado.isEmpty())
    }*/
}