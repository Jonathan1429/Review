package com.jonathanev.review.Data

import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Data.Model.PreguntaRespuestaModel
import com.jonathanev.review.Domain.GetAllGuiasUseCase
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.File
import junit.framework.TestCase.assertTrue
import org.xmlpull.v1.XmlSerializer
import java.io.FileOutputStream

class GuiaRepositoryIntegrationTest() {
    private val getAllGuiasUseCase = GetAllGuiasUseCase(mockk(relaxed = true))
    private val guiaProvider = GuiaProvider()
    private val xmlSerializerFactory = XmlSerializerFactory()
    private val fileOutputStreamFactory = FileOutputStreamFactory()

    private val repository = GuiaRepository(
        getAllGuiasUseCase,
        guiaProvider,
        xmlSerializerFactory,
        fileOutputStreamFactory
    )

    @Test
    fun guarda_y_lee_XML_correctamente() {
        val preguntas = arrayListOf("Pregunta1", "Pregunta2")
        val respuestas = arrayListOf("Respuesta1", "Respuesta2")

        val tempFile = File.createTempFile("guia_integration", ".xml")
        val ruta = tempFile.absolutePath

        // Guardar archivo
        val validacion = repository.saveFile(
            "archivo.xml",
            preguntas,
            respuestas,
            didTheGuideAlreadyExist = false,
            ruta = ruta
        )

        assertEquals("Guia de estudio creada exitosamente", validacion.message)
        assertTrue(tempFile.exists())

        // Leer archivo
        val resultado = repository.obtenerDatosXML("archivo.xml", ruta)
        val esperado = listOf(
            PreguntaRespuestaModel("Pregunta1", "Respuesta1"),
            PreguntaRespuestaModel("Pregunta2", "Respuesta2")
        )

        assertEquals(esperado, resultado)

        // Limpieza
        tempFile.delete()
    }
}
