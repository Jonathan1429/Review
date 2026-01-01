package com.jonathanev.review.UIs.ViewModel

import android.graphics.Color
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.jonathanev.review.presentation.viewmodel.DialogColorsPopUpViewModel
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class _DialogColorsPopUpViewModelTest {
    // Esto hace que LiveData se ejecute inmediatamente en los tests unitarios
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DialogColorsPopUpViewModel

    @Before
    fun setup() {
        // Inicializamos el ViewModel antes de cada test
        viewModel = DialogColorsPopUpViewModel()
    }

    @Test
    fun `setColor should update LiveData`() {
        // Creamos un observer relajado (no hace nada pero podemos verificar llamadas)
        val observer = mockk<Observer<Int>>(relaxed = true)
        viewModel.colorSeleccionado.observeForever(observer)

        val color = Color.RED

        // Act: llamamos setColor con un valor
        viewModel.setColor(color)

        // Assert: verificamos que el observer recibió el color correcto
        verify { observer.onChanged(color) }
    }

    @Test
    fun `resetColor should update LiveData to black`() {
        val observer = mockk<Observer<Int>>(relaxed = true)
        viewModel.colorSeleccionado.observeForever(observer)

        // Act: llamamos resetColor
        viewModel.resetColor()

        // Assert: verificamos que el color actualizado sea negro
        verify { observer.onChanged(Color.BLACK) }
    }
}