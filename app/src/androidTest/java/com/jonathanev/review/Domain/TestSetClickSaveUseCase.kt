package com.jonathanev.review.Domain

import android.text.SpannableStringBuilder
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ResponseGuia
import com.jonathanev.review.Data.Model.SpanPalabraModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestSetClickSaveUseCase {
    /*private val setSpanPalabraUseCase = mockk<SetSpanPalabraUseCase>()
    private val setColocarEtiquetasUseCase = mockk<SetColocarEtiquetasUseCase>()
    private val setCrearXmlUseCase = mockk<SetCrearXmlUseCase>()
    private val setClickSaveUseCase = SetClickSaveUseCase(setSpanPalabraUseCase, setColocarEtiquetasUseCase, setCrearXmlUseCase)

    @Test
    fun mostrar_mensaje_que_debe_de_llenar_pregunta_y_respuesta_cuando_vas_a_sobreescribir_algo_vacio(){
        val preguntas = arrayListOf("b", "c")
        val respuestas = arrayListOf("b", "c")
        val contador = 1
        val editable = SpannableStringBuilder("")
        val nombreArchivo = "a"

        every { setSpanPalabraUseCase.invoke(editable) } returns
                SpanPalabraModel(
                    editable = editable,
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(editable) } returns editable

        val respuesta = setClickSaveUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            nombreArchivo = nombreArchivo,
            isEtPregunta = true,
            didTheGuideAlreadyExist = false,
            ruta = "sd/guias"
        )

        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta",
                estadoUI = EstadoUI(isUpdatedAskAns = false)
            ), respuesta
        )
    }

    @Test
    fun mostrar_mensaje_que_debe_de_llenar_pregunta_y_respuesta_cuando_vas_a_guardar_algo_vacio(){
        val preguntas = arrayListOf("b", "c")
        val respuestas = arrayListOf("b")
        val contador = 1
        val editable = SpannableStringBuilder("")
        val nombreArchivo = "a"

        every { setSpanPalabraUseCase.invoke(editable) } returns
                SpanPalabraModel(
                    editable = editable,
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(editable) } returns editable

        val respuesta = setClickSaveUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            nombreArchivo = nombreArchivo,
            isEtPregunta = false,
            didTheGuideAlreadyExist = false,
            ruta = "sd/guias"
        )

        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta",
                estadoUI = EstadoUI(isUpdatedAskAns = false)
            ), respuesta
        )
    }

    @Test
    fun despues_de_darle_next__darle_guardar_y_lo_haga_bien(){
        val preguntas = arrayListOf("b", "c")
        val respuestas = arrayListOf("b", "c")
        val contador = 2
        val editable = SpannableStringBuilder("")
        val nombreArchivo = "a"

        every { setSpanPalabraUseCase.invoke(editable) } returns
                SpanPalabraModel(
                    editable = editable,
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(editable) } returns editable

        every { setCrearXmlUseCase.invoke(nombreArchivo, preguntas, respuestas, false, ruta = "sd/guias") } returns
                ValidacionesGuiaModel(
                    message = "Guia de estudio creada exitosamente",
                    responseGuia = ResponseGuia("sd/guias"),
                    estadoUI = EstadoUI(
                        isCreatedGuia = true,
                    )
                )

        val respuesta = setClickSaveUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            nombreArchivo = nombreArchivo,
            isEtPregunta = true,
            didTheGuideAlreadyExist = false,
            ruta = "sd/guias"
        )

        assertEquals(listOf("b", "c"), preguntas)
        assertEquals(listOf("b", "c"), respuestas)

        assertEquals(
            ValidacionesGuiaModel(
                message = "Guia de estudio creada exitosamente",
                responseGuia = ResponseGuia("sd/guias"),
                estadoUI = EstadoUI(
                    isCreatedGuia = true,
                )
            ), respuesta
        )
    }

    @Test
    fun guardar_el_xml_sobreescribiendo_la_pregunta(){
        val preguntas = arrayListOf("b", "c")
        val respuestas = arrayListOf("b", "c")
        val contador = 1
        val editable = SpannableStringBuilder("x")
        val nombreArchivo = "a"

        every { setSpanPalabraUseCase.invoke(editable) } returns
                SpanPalabraModel(
                    editable = editable,
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(editable) } returns editable

        every { setCrearXmlUseCase.invoke(nombreArchivo, preguntas, respuestas, false, ruta = "sd/guias") } returns
                ValidacionesGuiaModel(
                    message = "Guia de estudio creada exitosamente",
                    responseGuia = ResponseGuia("sd/guias"),
                    estadoUI = EstadoUI(
                        isCreatedGuia = true,
                    )
                )

        val respuesta = setClickSaveUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            nombreArchivo = nombreArchivo,
            isEtPregunta = true,
            didTheGuideAlreadyExist = false,
            ruta = "sd/guias"
        )

        assertEquals(listOf("b", "x"), preguntas)
        assertEquals(listOf("b", "c"), respuestas)

        assertEquals(
            ValidacionesGuiaModel(
                message = "Guia de estudio creada exitosamente",
                responseGuia = ResponseGuia("sd/guias"),
                estadoUI = EstadoUI(
                    isCreatedGuia = true,
                )
            ), respuesta
        )
    }

    @Test
    fun guardar_el_xml_sobreescribiendo_la_respuesta(){
        val preguntas = arrayListOf("b", "c")
        val respuestas = arrayListOf("b", "c")
        val contador = 1
        val editable = SpannableStringBuilder("x")
        val nombreArchivo = "a"

        every { setSpanPalabraUseCase.invoke(editable) } returns
                SpanPalabraModel(
                    editable = editable,
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(editable) } returns editable

        every { setCrearXmlUseCase.invoke(nombreArchivo, preguntas, respuestas, false, ruta = "sd/guias") } returns
                ValidacionesGuiaModel(
                    message = "Guia de estudio creada exitosamente",
                    responseGuia = ResponseGuia("sd/guias"),
                    estadoUI = EstadoUI(
                        isCreatedGuia = true,
                    )
                )

        val respuesta = setClickSaveUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            nombreArchivo = nombreArchivo,
            isEtPregunta = false,
            didTheGuideAlreadyExist = false,
            ruta = "sd/guias"
        )

        assertEquals(listOf("b", "c"), preguntas)
        assertEquals(listOf("b", "x"), respuestas)

        assertEquals(
            ValidacionesGuiaModel(
                message = "Guia de estudio creada exitosamente",
                responseGuia = ResponseGuia("sd/guias"),
                estadoUI = EstadoUI(
                    isCreatedGuia = true,
                )
            ), respuesta
        )
    }

    @Test
    fun guardar_el_xml_escribiendo_la_respuesta(){
        val preguntas = arrayListOf("b", "c")
        val respuestas = arrayListOf("b")
        val contador = 1
        val editable = SpannableStringBuilder("x")
        val nombreArchivo = "a"

        every { setSpanPalabraUseCase.invoke(editable) } returns
                SpanPalabraModel(
                    editable = editable,
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(editable) } returns editable

        every { setCrearXmlUseCase.invoke(nombreArchivo, preguntas, respuestas, false, ruta = "sd/guias") } returns
                ValidacionesGuiaModel(
                    message = "Guia de estudio creada exitosamente",
                    responseGuia = ResponseGuia("sd/guias"),
                    estadoUI = EstadoUI(
                        isCreatedGuia = true,
                    )
                )

        val respuesta = setClickSaveUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            nombreArchivo = nombreArchivo,
            isEtPregunta = false,
            didTheGuideAlreadyExist = false,
            ruta = "sd/guias"
        )

        assertEquals(listOf("b", "c"), preguntas)
        assertEquals(listOf("b", "x"), respuestas)

        assertEquals(
            ValidacionesGuiaModel(
                message = "Guia de estudio creada exitosamente",
                responseGuia = ResponseGuia("sd/guias"),
                estadoUI = EstadoUI(
                    isCreatedGuia = true,
                )
            ), respuesta
        )
    }*/

    /*@Test
    fun guardar_el_xml_sobreescribiendo_la_respuesta(){
        val preguntas = arrayListOf("b, c")
        val respuestas = arrayListOf("b")
        val contador = 1
        val editable = SpannableStringBuilder("x")
        val nombreArchivo = "a"

        every { setSpanPalabraUseCase.invoke(editable) } returns
                SpanPalabraModel(
                    editable = editable,
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(editable) } returns editable

        every { setCrearXmlUseCase.invoke(nombreArchivo, preguntas, respuestas, false, ruta = "sd/guias") } returns
                ValidacionesGuiaModel(
                    message = "Guia de estudio creada exitosamente",
                    responseGuia = ResponseGuia("sd/guias"),
                    estadoUI = EstadoUI(
                        isCreatedGuia = true,
                    )
                )

        val respuesta = setClickSaveUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            nombreArchivo = nombreArchivo,
            isEtPregunta = false,
            didTheGuideAlreadyExist = false,
            ruta = "sd/guias"
        )

        assertEquals(
            ValidacionesGuiaModel(
                message = "Guia de estudio creada exitosamente",
                responseGuia = ResponseGuia("sd/guias"),
                estadoUI = EstadoUI(
                    isCreatedGuia = true,
                )
            ), respuesta
        )
    }*/
}