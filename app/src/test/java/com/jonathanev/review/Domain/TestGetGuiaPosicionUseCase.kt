package com.jonathanev.review.Domain

import com.jonathanev.review.Data.provider.GuiaProvider
import io.mockk.mockk

class TestGetGuiaPosicionUseCase {
    private val guiaProvider = mockk<GuiaProvider>()
    private var getFolderPosicionUseCase = GetFolderPosicionUseCase()

    /*@Test
    fun `te regresa la guia en la posicion enviada que se encuentra en el provider`() {
        val guias = mockk<GuiaProvider>()
        every { guiaProvider.guias } returns
                listOf(
                    GuiaModel("a", 0, true),
                    GuiaModel("b", 0, true)
                )

        getGuiaPosicionUseCase = GetGuiaPosicionUseCase()
        //val resultado = getGuiaPosicionUseCase(0, guias)
        //assertEquals(GuiaModel("a", 0, true), resultado)
    }*/
}