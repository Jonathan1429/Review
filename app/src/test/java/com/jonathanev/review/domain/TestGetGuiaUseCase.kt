package com.jonathanev.review.domain

class TestGetGuiaUseCase {
    /*private val guiaProvider = mockk<GuiaProvider>()
    private val getGuiaUseCase = GetGuiaUseCase(guiaProvider)

    @Test
    fun `cuando la ruta contiene un archivo existente, devuelve el GuiaModel correcto`() {
        // Arrange
        val guias = listOf(
            GuiaModel("GuiaA", 1),
            GuiaModel("GuiaB", 2)
        )
        every { guiaProvider.guias } returns guias

        val ruta = "/storage/emulated/0/GuiaB.xml"

        // Act
        val resultado = getGuiaUseCase(ruta)

        // Assert
        assertEquals("GuiaB", resultado.nombreGuia)
    }*/

    /*@Test
    fun `cuando la ruta no coincide con ninguna guía, devuelve un GuiaModel vacío`() {
        // Arrange
        val guias = listOf(
            GuiaModel("GuiaA", 1),
            GuiaModel("GuiaB", 2)
        )
        every { guiaProvider.guias } returns guias

        val ruta = "/storage/emulated/0/GuiaC.xml"

        // Act
        val resultado = getGuiaUseCase(ruta)

        // Assert
        assertEquals("", resultado.nombreGuia)
    }*/
}