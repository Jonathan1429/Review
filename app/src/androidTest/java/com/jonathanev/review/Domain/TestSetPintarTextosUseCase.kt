package com.jonathanev.review.Domain

import android.text.style.ForegroundColorSpan
import com.jonathanev.review.Data.repository.FileHelperImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Type

class TestSetPintarTextosUseCase {
    /*private val setCifrarRutaImagenUseCase = mockk<SetCifrarRutaImagenUseCase>()
    private val fileHelper = mockk<FileHelperImpl>()
    private val setPintarTextosUseCase = SetPintarTextosUseCase(setCifrarRutaImagenUseCase, fileHelper)

    @Test
    fun cuando_es_pregunta_simple_sin_etiquetas_devuelve_builder_sin_imagen() {
        val preguntas = arrayListOf("Hola mundo")
        val respuestas = arrayListOf<String>()

        val result = setPintarTextosUseCase(
            isEtPregunta = true,
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = 0,
            ruta = "guias/miRuta/"
        )

        //assertNotNull(result.builder)
        assertTrue(result.estadoUI.isUpdatedAskAns)
        assertTrue(result.estadoUI.isShowQuitColor)
        assertTrue(result.estadoUI.isShowSelColor)
        //assertNull(result.estadoImagen)
    }

    @Test
    fun cuando_es_pregunta_con_etiquetas_de_color_aplica_colores_correctamente() {
        val preguntas = arrayListOf("Esto es «255»rojo«» y «65280»verde«»")
        val respuestas = arrayListOf<String>()

        val result = setPintarTextosUseCase(
            isEtPregunta = true,
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = 0,
            ruta = "guias/miRuta/"
        )

        //assertNotNull(result.builder)
        val spans = result.builder!!.getSpans(0, result.builder!!.length, ForegroundColorSpan::class.java)
        assertEquals(2, spans.size)
        assertEquals(255, spans[0].foregroundColor)
        assertEquals(65280, spans[1].foregroundColor)
        //assertNull(result.estadoImagen)
    }

    @Test
    fun cuando_texto_contiene_imagen_y_archivo_existe_devuelve_estado_con_imagen() {
        val rutaOriginal = "content://media/picker/guias/40.png"
        val textoImagen = "frqwhqw://phgld/slfnhu/fjxdbkbp/40.mkd"
        val preguntas = arrayListOf(textoImagen)
        val respuestas = arrayListOf<String>()

        // Simulamos cifrado
        every { setCifrarRutaImagenUseCase(any(), any()) } returns "content://media/picker/imagenes/40.png"
        // Simulamos que el archivo existe
        every { fileHelper.exists(any()) } returns true

        val result = setPintarTextosUseCase(
            isEtPregunta = true,
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = 0,
            ruta = rutaOriginal
        )

        assertTrue(result.estadoUI.typeFile == TypeFile.IMAGEN)
        //assertNotNull(result.estadoImagen)
        assertEquals("content://media/picker/imagenes/40.png", result.estadoImagen.textImgUnencrypted)
    }

    @Test
    fun cuando_texto_contiene_imagen_y_archivo_no_existe_usa_imagenPivote() {
        val rutaOriginal = "content://media/picker/guias/40.png"
        val textoImagen = "frqwhqw://phgld/slfnhu/fjxdbkbp/40.mkd"
        val preguntas = arrayListOf(textoImagen)
        val respuestas = arrayListOf<String>()

        every { setCifrarRutaImagenUseCase(any(), any()) } returns "content://media/picker/imagenes/40.png"
        // Simulamos que el archivo no existe
        every { fileHelper.exists(any()) } returns false

        val result = setPintarTextosUseCase(
            isEtPregunta = true,
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = 0,
            ruta = rutaOriginal
        )

        assertTrue(result.estadoUI.typeFile == TypeFile.IMAGEN)
        assertTrue(result.estadoImagen.textImgUnencrypted.contains("imagenesPivote"))
    }

    @Test
    fun cuando_es_respuesta_con_etiquetas_de_color_aplica_colores_correctamente() {
        val preguntas = arrayListOf<String>()
        val respuestas = arrayListOf("Hola «255»mundo«»")

        val result = setPintarTextosUseCase(
            isEtPregunta = false,
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = 0,
            ruta = "guias/miRuta/"
        )

        //assertNotNull(result.builder)
        val spans = result.builder!!.getSpans(0, result.builder!!.length, ForegroundColorSpan::class.java)
        assertEquals(1, spans.size)
        assertEquals(255, spans[0].foregroundColor)
        //assertNull(result.estadoImagen)
    }*/
}