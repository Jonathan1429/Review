package com.jonathanev.review.Domain

import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.PreguntaRespuestaModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TestGetObtenerDatosXMLUseCase {
    //private val guiaProvider
    private val guiaRepository = mockk<GuiaRepository>()
    private val getObtenerDatosXMLUseCase = GetObtenerDatosXMLUseCase(guiaRepository)

    @Test
    fun `Obtener los datos del xml del archivo mandado`() {
        val nombreArchivo = "A.xml"
        val ruta = "storage/"
        val preguntasRespuestasEsperadas = listOf(
            PreguntaRespuestaModel("¿Qué es Kotlin?", "Un lenguaje de programación"),
            PreguntaRespuestaModel("¿Qué es Android?", "Un sistema operativo")
        )

        every {
            guiaRepository.obtenerDatosXML(
                nombreArchivo,
                ruta
            )
        } returns preguntasRespuestasEsperadas

        val resultado = getObtenerDatosXMLUseCase(nombreArchivo, ruta)

        // Assert
        assertEquals(preguntasRespuestasEsperadas, resultado)
    }

    @Test
    fun `Sino se encuentra la guia solicitada te regresa una lista vacia`() {
        val nombreArchivo = "A.xml"
        val ruta = "storage/"

        every {
            guiaRepository.obtenerDatosXML(
                nombreArchivo,
                ruta
            )
        } returns emptyList()

        val resultado = getObtenerDatosXMLUseCase(nombreArchivo, ruta)

        // Assert
        assertTrue(resultado.isEmpty())
    }
}