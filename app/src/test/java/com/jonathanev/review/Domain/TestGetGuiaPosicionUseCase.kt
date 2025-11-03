package com.jonathanev.review.Domain

import com.jonathanev.review.Data.Model.GuiaModel
import com.jonathanev.review.Data.provider.GuiaProvider
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestGetGuiaPosicionUseCase {
    private val guiaProvider = mockk<GuiaProvider>()
    private var getGuiaPosicionUseCase = GetGuiaPosicionUseCase()

    @Test
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
    }
}