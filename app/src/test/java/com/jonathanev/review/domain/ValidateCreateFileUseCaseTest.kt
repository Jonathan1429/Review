package com.jonathanev.review.domain

import com.jonathanev.review.domain.result.ValidateCreateFileResult
import com.jonathanev.review.presentation.model.FileFormMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateCreateFileUseCaseTest {
    private val validateCreateFileUseCase = ValidateCreateFileUseCase()
    private val fileFormMode = FileFormMode.CreatingFile

    @Test
    fun validacion_vacia() {
        val resultado = validateCreateFileUseCase.invoke("", "", fileFormMode)

        assertEquals(ValidateCreateFileResult.Error("Debes tener un nombre de archivo"), resultado)
    }

    @Test
    fun validacion_sin_caracteres_raros() {
        val resultado = validateCreateFileUseCase.invoke("hola.mx", "", fileFormMode)
        assertEquals(
            ValidateCreateFileResult.Error("Solo se permiten letras, números, espacios y guiones"),
            resultado
        )

        val resultado2 = validateCreateFileUseCase.invoke("hola/m/x", "", fileFormMode)
        assertEquals(
            ValidateCreateFileResult.Error("Solo se permiten letras, números, espacios y guiones"),
            resultado2
        )
    }

    @Test
    fun validacion_con_nombres_restringidos() {
        val resultado = validateCreateFileUseCase.invoke("guias", "", fileFormMode)
        assertEquals(ValidateCreateFileResult.Error("Ese nombre no está permitido"), resultado)
    }

    @Test
    fun validacion_aceptada() {
        val name = "prueba2"
        val descripcion = "descripcion de prueba"
        val resultado = validateCreateFileUseCase.invoke(name, descripcion, fileFormMode)
        assertEquals(ValidateCreateFileResult.Success(name, descripcion), resultado)
    }
}