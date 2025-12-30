package com.jonathanev.review.data

class GuiaRepositoryIntegrationTest() {
    /*private val getAllGuiasUseCase = GetAllGuiasUseCase(mockk(relaxed = true))
    private val guiaProvider = GuiaProvider()
    private val xmlSerializerFactory = XmlSerializerFactory()
    private val fileOutputStreamFactory = FileOutputStreamFactory()
    private val filePathsProvider = FilePathsProvider(mockk())

    private val repository = GuiaRepository(
        getAllGuiasUseCase,
        guiaProvider,
        xmlSerializerFactory,
        fileOutputStreamFactory,
        filePathsProvider
    )

    @Test
    fun guarda_y_lee_XML_correctamente() {
        val preguntas = arrayListOf("Pregunta1", "Pregunta2")
        val respuestas = arrayListOf("Respuesta1", "Respuesta2")

        val tempFile = File.createTempFile("guia_integration", ".xml")
        val ruta = tempFile.absolutePath

        // Guardar archivo
        val validacion = repository.saveFileV1(
            "archivo.xml",
            preguntas,
            respuestas,
            didTheGuideAlreadyExist = false,
            ruta = ruta
        )

        assertEquals("Guia de estudio creada exitosamente", validacion.message)
        assertTrue(tempFile.exists())

        // Leer archivo
        val resultado = repository.obtenerDatosXMLV1(ruta)
        val esperado = listOf(
            QuestionAnswerModel("Pregunta1", "Respuesta1"),
            QuestionAnswerModel("Pregunta2", "Respuesta2")
        )

        assertEquals(esperado, resultado)

        // Limpieza
        tempFile.delete()
    }*/
}
