package com.jonathanev.review.data

class GuiaRepositoryUnitTest {
    /*private val getAllGuiasUseCase = mockk<GetAllGuiasUseCase>()
    private val guiaProvider = GuiaProvider()
    private val xmlSerializerFactory = mockk<XmlSerializerFactory>()
    private val fileOutputStreamFactory = mockk<FileOutputStreamFactory>()
    private val filePathsProvider = FilePathsProvider(mockk())

    private val repository = GuiaRepository(
        getAllGuiasUseCase,
        guiaProvider,
        xmlSerializerFactory,
        fileOutputStreamFactory,
        filePathsProvider
    )

    // ======= getGuias branch
    @Test
    fun getGuias_retorna_lista_del_usecase() {
        val fileMock = mockk<File>()
        val listaEsperada = listOf(GuiaModel("A", 1, true))

        every { getAllGuiasUseCase.invoke(fileMock) } returns listaEsperada

        val resultado = repository.getGuias(fileMock)

        assertEquals(listaEsperada, resultado)
    }

    // ======= saveFile branch: IOException
    @Test
    fun saveFile_V1_captura_IOException_y_retorna_error() {
        every { xmlSerializerFactory.create() } throws java.io.IOException("error")

        val resultado = repository.saveFileV1(
            "archivo.xml",
            arrayListOf(),
            arrayListOf(),
            didTheGuideAlreadyExist = false,
            ruta = "/ruta/falsa.xml"
        )

        assertEquals("Guia de estudio no se creó correctamente", resultado.message)
    }

    // ======= saveFile branch: archivo previo eliminado
    @Test
    fun saveFile_V1_elimina_archivo_existente_antes_de_crear() {
        val tempFile = File.createTempFile("guia_test", ".xml")
        tempFile.writeText("algo")

        val serializerMock = mockk<XmlSerializer>(relaxed = true)
        val fosMock = mockk<FileOutputStream>(relaxed = true)

        every { xmlSerializerFactory.create() } returns serializerMock
        every { fileOutputStreamFactory.create(tempFile.absolutePath) } returns fosMock

        val resultado = repository.saveFileV1(
            "archivo.xml",
            arrayListOf("P1"),
            arrayListOf("R1"),
            didTheGuideAlreadyExist = true,
            ruta = tempFile.absolutePath
        )

        assertEquals("Guia de estudio creada exitosamente", resultado.message)
    }

    // ======= obtenerDatosXML branch: ruta == "null" y excepciones
    @Test
    fun obtenerDatosXMLV1_rutaNull_y_captura_excepciones() {
        val nombreArchivo = "archivo.xml"

        // Forzamos que el archivo no exista y que se lance IOException al parsear
        //val resultado = repository.obtenerDatosXML(nombreArchivo, "null")

        // Debe regresar lista vacía
        //assertEquals(emptyList<PreguntaRespuestaModel>(), resultado)
    }

    /*@Test
    fun getGuias_actualiza_y_retorna_guias_del_usecase() {
        val fileMock = mockk<File>()
        val listaEsperada = listOf(GuiaModel("GuiaX", 2, false))

        every { getAllGuiasUseCase.invoke(fileMock) } returns listaEsperada

        val resultado = repository.getGuias(fileMock)

        // Se asignó al provider
        assertEquals(listaEsperada, guiaProvider.guias)
        // Se retorna la misma lista
        assertEquals(listaEsperada, resultado)
    }*/

    @Test
    fun obtenerDatosXMLV1_captura_ParserConfigurationException() {
        mockkStatic(DocumentBuilderFactory::class)
        val dbfMock = mockk<DocumentBuilderFactory>()
        every { DocumentBuilderFactory.newInstance() } returns dbfMock
        every { dbfMock.newDocumentBuilder() } throws javax.xml.parsers.ParserConfigurationException("config error")

        //val resultado = repository.obtenerDatosXML("archivo.xml", "/ruta/falsa.xml")

        //assertEquals(emptyList<PreguntaRespuestaModel>(), resultado)

        unmockkStatic(DocumentBuilderFactory::class)
    }

    /*@Test
    fun usa_directamente_getAllGuiasUseCase_y_guiaProvider() {
        val mockGetAll = mockk<GetAllGuiasUseCase>()
        val mockProvider = GuiaProvider()
        val repo = GuiaRepository(mockGetAll, mockProvider, mockk(), mockk())

        val archivoMock = mockk<File>()
        val guiasMock = listOf(GuiaModel("A", 0, false))
        every { mockGetAll.invoke(archivoMock) } returns guiasMock

        val resultado = repo.getGuias(archivoMock)

        // Esto fuerza a usar las propiedades directamente
        assertEquals(guiasMock, resultado)
        assertEquals(guiasMock, mockProvider.guias)
    }*/

    @Test
    fun captura_SAXException_y_no_falla() {
        val repo = GuiaRepository(
            mockk(relaxed = true),
            GuiaProvider(),
            mockk(),
            mockk(),
            mockk()
        )

        // Archivo temporal inválido
        val tempFile = File.createTempFile("invalido", ".xml")
        tempFile.writeText("<GuiaEstudio><Cuestionario><Interrogante></Cuestionario>") // XML mal formado

        //val resultado = repo.obtenerDatosXML("archivo.xml", tempFile.absolutePath)

        // Verificación: la función devuelve lista vacía sin lanzar excepción
        //assertTrue(resultado.isEmpty())

        tempFile.delete()
    }*/

    /*@Test
    fun usa_getAllGuiasUseCase_y_guiaProvider_explicitamente() {
        val mockGetAll = mockk<GetAllGuiasUseCase>()
        val mockProvider = GuiaProvider()
        val repo = GuiaRepository(mockGetAll, mockProvider, mockk(), mockk())

        val archivoMock = mockk<File>()
        val guiasMock = listOf(GuiaModel("A", 0, false))

        // Configura el mock para devolver algo
        every { mockGetAll.invoke(archivoMock) } returns guiasMock

        // Llamada que usa las dos propiedades
        val resultado = repo.getGuias(archivoMock)

        // Verificación
        assertEquals(guiasMock, resultado)
        assertEquals(guiasMock, mockProvider.guias)
    }*/
}