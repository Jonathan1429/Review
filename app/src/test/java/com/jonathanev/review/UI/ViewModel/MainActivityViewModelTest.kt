package com.jonathanev.review.UI.ViewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class MainActivityViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // Esto fuerza que LiveData corra en el hilo de prueba

    @Test
    fun `getAllGuias actualiza guias correctamente`() {
        // Mocks
        val repository = mockk<GuiaRepository>()
        val viewModel = MainActivityViewModel(repository)
        val archivoMock = mockk<File>()
        val guiasMock = listOf(
            GuiaModel("Guia1", 1, true),
            GuiaModel("Guia2", 2, false)
        )

        // Configuramos el mock del repositorio
        every { repository.getGuias(archivoMock) } returns guiasMock

        // Llamamos a la función
        viewModel.getAllGuias(archivoMock)

        // Verificamos que el LiveData se actualizó
        assertEquals(guiasMock, viewModel.guias.value)
    }
}