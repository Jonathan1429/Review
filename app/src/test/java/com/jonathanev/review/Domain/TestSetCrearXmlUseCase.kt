package com.jonathanev.review.Domain

import com.jonathanev.review.Data.FileOutputStreamFactory
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.Model.GuiaProvider
import com.jonathanev.review.Data.Model.ResponseGuia
import com.jonathanev.review.Data.XmlSerializerFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.xmlpull.v1.XmlSerializer
import java.io.FileOutputStream

class TestSetCrearXmlUseCase {
    private val getAllGuiasUseCase = mockk<GetAllGuiasUseCase>()
    private val guiaProvider       = mockk<GuiaProvider>()
    private val serializerFactory  = mockk<XmlSerializerFactory>()
    private val fosFactory         = mockk<FileOutputStreamFactory>()
    private val serializer         = mockk<XmlSerializer>(relaxed = true)
    private val fos                = mockk<FileOutputStream>(relaxed = true)

    private val repo = GuiaRepository(
        getAllGuiasUseCase,
        guiaProvider,
        serializerFactory,
        fosFactory
    )
    private val useCase = SetCrearXmlUseCase(repo)

    @Before
    fun setUp() {
        every { serializerFactory.create() } returns serializer
        every { fosFactory.create(any()) }   returns fos
    }

    @Test
    fun `crear el xml exitosamente sino existe`() {
        val nombreArchivo    = "a.xml"
        val preguntas = arrayListOf("¿Qué es Kotlin?")
        val respuestas= arrayListOf("Un lenguaje de programación.")
        val result = useCase(nombreArchivo, preguntas, respuestas, true, "/ruta/fake.xml")

        // validaciones del modelo devuelto
        assertEquals("Guia de estudio creada exitosamente", result.message)
        assertEquals(ResponseGuia("/ruta/fake.xml"), result.responseGuia)
        assertTrue(result.estadoUI.isCreatedGuia)

        // y que se invocó correctamente al serializer
        verify {
            serializer.setOutput(fos, "UTF-8")
            serializer.startDocument(null, true)
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag("", "GuiaEstudio")
            serializer.attribute("", "version", "1.0")
            serializer.startTag("", "Cuestionario")
            serializer.attribute("", "nombreGuia", nombreArchivo)

            // Creo la etiqueta interrogante con su respectiva pregunta
            for (i in preguntas.indices) {
                serializer.startTag("", "Interrogante")
                serializer.attribute("", "pregunta", preguntas[i])
                serializer.attribute("", "respuesta", respuestas[i])
                serializer.endTag("", "Interrogante")
            }

            // Si los campos estan vacios simplemente cierro las etiquetas y directamente
            // guardo el documento en el teléfono.
            serializer.endTag("", "Cuestionario")
            serializer.endTag("", "GuiaEstudio")
            serializer.endDocument()
            serializer.flush()
            fos.close()
        }
    }

    @Test
    fun `crear el xml exitosamente si existe`() {
        val nombreArchivo    = "a.xml"
        val preguntas = arrayListOf("¿Qué es Kotlin?")
        val respuestas= arrayListOf("Un lenguaje de programación.")
        val result = useCase(nombreArchivo, preguntas, respuestas, false, "/ruta/fake.xml")

        // validaciones del modelo devuelto
        assertEquals("Guia de estudio creada exitosamente", result.message)
        assertEquals(ResponseGuia("/ruta/fake.xml"), result.responseGuia)
        assertTrue(result.estadoUI.isCreatedGuia)

        // y que se invocó correctamente al serializer
        verify {
            serializer.setOutput(fos, "UTF-8")
            serializer.startDocument(null, true)
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.startTag("", "GuiaEstudio")
            serializer.attribute("", "version", "1.0")
            serializer.startTag("", "Cuestionario")
            serializer.attribute("", "nombreGuia", nombreArchivo)

            // Creo la etiqueta interrogante con su respectiva pregunta
            for (i in preguntas.indices) {
                serializer.startTag("", "Interrogante")
                serializer.attribute("", "pregunta", preguntas[i])
                serializer.attribute("", "respuesta", respuestas[i])
                serializer.endTag("", "Interrogante")
            }

            // Si los campos estan vacios simplemente cierro las etiquetas y directamente
            // guardo el documento en el teléfono.
            serializer.endTag("", "Cuestionario")
            serializer.endTag("", "GuiaEstudio")
            serializer.endDocument()
            serializer.flush()
            fos.close()
        }
    }
}