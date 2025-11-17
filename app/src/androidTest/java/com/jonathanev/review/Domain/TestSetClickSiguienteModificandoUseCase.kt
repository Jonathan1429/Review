package com.jonathanev.review.Domain

import android.text.Editable
import android.text.SpannableStringBuilder
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.SpanPalabraModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestSetClickSiguienteModificandoUseCase {
    /*private val setSpanPalabraUseCase = mockk<SetSpanPalabraUseCase>()
    private val setColocarEtiquetasUseCase = mockk<SetColocarEtiquetasUseCase>()
    private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val setClickSiguienteModicandoUseCase = SetClickSiguienteModificandoUseCase(
        setSpanPalabraUseCase,
        setColocarEtiquetasUseCase,
        setPintarTextosUseCase
    )

    @Test
    fun das_click_siguiente_sino_hay_mas_te_regresa_un_mensaje_que_no_hay_mas_preguntas() {
        val preguntas = ArrayList<String>()
        val respuestas = ArrayList<String>()
        val contadorPregunta = 0
        val editable: Editable = Editable.Factory.getInstance().newEditable("")
        val ruta = ""

        val resultado = setClickSiguienteModicandoUseCase(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contadorPregunta,
            editable = editable,
            isEtPregunta = false,
            ruta
        )

        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta",
            ), resultado
        )
    }

    @Test
    fun vamos_a_pintar_el_texto_siguiente_y_guardar_lo_anterior_en_pregunta() {
        val preguntas = arrayListOf("a", "b")
        val respuestas = arrayListOf("a", "b")
        val contadorPregunta = 0
        val editable = SpannableStringBuilder("x")
        val ruta = ""

        every { setSpanPalabraUseCase.invoke(any()) } returns
                SpanPalabraModel(
                    editable = SpannableStringBuilder("x"),
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(any()) } returns
                SpannableStringBuilder("x")

        every { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("x"),
                )

        val resultado = setClickSiguienteModicandoUseCase.invoke(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contadorPregunta,
            editable = editable,
            isEtPregunta = true,
            ruta
        )

        assertEquals(arrayListOf("x", "b"), preguntas)
        assertEquals(arrayListOf("a", "b"), respuestas)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("x"),
                responseSpanPalabra = SpanPalabraModel(
                    editable = editable,
                    message = "",
                ),
            ), resultado
        )
    }

    @Test
    fun vamos_a_pintar_el_texto_siguiente_y_guardar_lo_anterior_en_respuesta() {
        val preguntas = arrayListOf("a", "b")
        val respuestas = arrayListOf("a", "b")
        val contadorPregunta = 0
        val editable = SpannableStringBuilder("x")
        val ruta = ""

        every { setSpanPalabraUseCase.invoke(any()) } returns
                SpanPalabraModel(
                    editable = SpannableStringBuilder("x"),
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(any()) } returns
                SpannableStringBuilder("x")

        every { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("x"),
                )

        val resultado = setClickSiguienteModicandoUseCase.invoke(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contadorPregunta,
            editable = editable,
            isEtPregunta = false,
            ruta
        )

        assertEquals(arrayListOf("a", "b"), preguntas)
        assertEquals(arrayListOf("x", "b"), respuestas)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("x"),
                responseSpanPalabra = SpanPalabraModel(
                    editable = editable,
                    message = "",
                ),
            ), resultado
        )
    }

    @Test
    fun guardar_lo_anterior_en_respuesta_y_ya_no_hay_mas_preguntas() {
        val preguntas = arrayListOf("a", "b")
        val respuestas = arrayListOf("a")
        val contadorPregunta = 1
        val editable = SpannableStringBuilder("x")
        val ruta = ""

        every { setSpanPalabraUseCase.invoke(any()) } returns
                SpanPalabraModel(
                    editable = SpannableStringBuilder("x"),
                    message = "",
                )

        every { setColocarEtiquetasUseCase.invoke(any()) } returns
                SpannableStringBuilder("x")

        every { setPintarTextosUseCase.invoke(any(), any(), any(), any(), any()) } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("x"),
                )

        val resultado = setClickSiguienteModicandoUseCase.invoke(
            preguntas = preguntas,
            respuestas = respuestas,
            contadorPregunta = contadorPregunta,
            editable = editable,
            isEtPregunta = false,
            ruta
        )

        assertEquals(arrayListOf("a", "b"), preguntas)
        assertEquals(arrayListOf("a", "x"), respuestas)
        assertEquals(
            ValidacionesGuiaModel(
                responseSpanPalabra = SpanPalabraModel(
                    editable = editable,
                    message = "",
                ),
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isClearText = true,
                    isShowQuitColor = true,
                    isShowSelColor = true
                )
            ), resultado
        )
    }*/
}