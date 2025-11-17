package com.jonathanev.review.Domain

import android.text.SpannableStringBuilder
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.SpanPalabraModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class TestSetRollClickedUseCase {
    /*private val setSpanPalabraUseCase = mockk<SetSpanPalabraUseCase>()
    private val setColocarEtiquetasUseCase = mockk<SetColocarEtiquetasUseCase>()
    private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val setRollClickedUseCase = SetRollClickedUseCase(
        setSpanPalabraUseCase,
        setColocarEtiquetasUseCase,
        setPintarTextosUseCase
    )

    @Test
    fun muestra_mensaje_de_llenar_pregunta_o_respuesta_si_esta_vacio_el_campo() {
        val preguntas = arrayListOf<String>()
        val respuestas = arrayListOf<String>()
        val contador = 0
        val editable = SpannableStringBuilder()
        val ruta = ""

        val resultado = setRollClickedUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            isEtPregunta = true,
            ruta
        )

        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta"
            ), resultado
        )
    }

    @Test
    fun guardar_pregunta_mostrar_vacia_la_respuesta() {
        val preguntas = arrayListOf<String>()
        val respuestas = arrayListOf<String>()
        val contador = 0
        val editable = SpannableStringBuilder("a")
        val ruta = ""

        every { setSpanPalabraUseCase.invoke(any()) } returns SpanPalabraModel(
            editable = editable,
            message = "",
        )

        every { setColocarEtiquetasUseCase.invoke(any()) } returns editable

        val resultado = setRollClickedUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            isEtPregunta = true,
            ruta
        )

        assertEquals(arrayListOf("a"), preguntas)
        assertEquals(arrayListOf<String>(), respuestas)

        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isEtPregunta = false
                ),
                responseSpanPalabra = SpanPalabraModel(
                    editable = editable,
                    message = "",
                )
            ), resultado
        )
    }

    @Test
    fun guardar_pregunta_mostrar_la_respuesta() {
        val preguntas = arrayListOf("a")
        val respuestas = arrayListOf("a")
        val contador = 0
        val editable = SpannableStringBuilder("b")
        val ruta = ""

        every { setSpanPalabraUseCase.invoke(any()) } returns SpanPalabraModel(
            editable = editable,
            message = "",
        )

        every { setColocarEtiquetasUseCase.invoke(any()) } returns editable

        every { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = editable,
                )

        val resultado = setRollClickedUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            isEtPregunta = true,
            ruta
        )

        assertEquals(arrayListOf("b"), preguntas)
        assertEquals(arrayListOf("a"), respuestas)

        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isEtPregunta = false
                ),
                responseSpanPalabra = SpanPalabraModel(
                    editable = editable,
                    message = "",
                ),
                builder = editable
            ), resultado
        )
    }

    @Test
    fun guardar_respuesta_mostrar_la_pregunta() {
        val preguntas = arrayListOf("a")
        val respuestas = arrayListOf("a")
        val contador = 0
        val editable = SpannableStringBuilder("b")
        val ruta = ""

        every { setSpanPalabraUseCase.invoke(any()) } returns SpanPalabraModel(
            editable = editable,
            message = "",
        )

        every { setColocarEtiquetasUseCase.invoke(any()) } returns editable

        every { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = editable,
                )

        val resultado = setRollClickedUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contador,
            editable = editable,
            isEtPregunta = false,
            ruta
        )

        assertEquals(arrayListOf("a"), preguntas)
        assertEquals(arrayListOf("b"), respuestas)

        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isEtPregunta = true
                ),
                responseSpanPalabra = SpanPalabraModel(
                    editable = editable,
                    message = "",
                ),
                builder = editable
            ), resultado
        )
    }*/
}