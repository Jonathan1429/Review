package com.jonathanev.review.domain

import com.jonathanev.review.domain.result.ValidateCreateFileResult
import com.jonathanev.review.presentation.state.CreatingFileUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateCreateFileUseCaseTest {
    private val validateCreateFileUseCase = ValidateCreateFileUseCase()

    @Test
    fun validacion_vacia() {
        val resultado = validateCreateFileUseCase.invoke("", "")

        assertEquals(ValidateCreateFileResult.Error("Debes tener un nombre de archivo"), resultado)
    }

    @Test
    fun validacion_sin_caracteres_raros() {
        val resultado = validateCreateFileUseCase.invoke("hola.mx", "")
        assertEquals(ValidateCreateFileResult.Error("No puede haber caracteres como / o . en el nombre"), resultado)

        val resultado2 = validateCreateFileUseCase.invoke("hola/m/x", "")
        assertEquals(ValidateCreateFileResult.Error("No puede haber caracteres como / o . en el nombre"), resultado2)
    }

    @Test
    fun validacion_con_nombres_restringidos() {
        val resultado = validateCreateFileUseCase.invoke("guias", "")
        assertEquals(ValidateCreateFileResult.Error("Ese nombre no está permitido"), resultado)
    }

    @Test
    fun validacion_aceptada() {
        val name = "prueba2"
        val descripcion = "descripcion de prueba"
        val resultado = validateCreateFileUseCase.invoke(name, descripcion)
        assertEquals(ValidateCreateFileResult.Success(name, descripcion), resultado)
    }
}