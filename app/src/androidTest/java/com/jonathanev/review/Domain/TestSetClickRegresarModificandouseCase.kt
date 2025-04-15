package com.jonathanev.review.Domain

import android.text.Editable
import android.text.SpannableStringBuilder
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.SpanPalabraModel
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestSetClickRegresarModificandouseCase {
    private val setSpanPalabraUseCase = mockk<SetSpanPalabraUseCase>()
    private val setColocarEtiquetasUseCase = mockk<SetColocarEtiquetasUseCase>()
    private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val setClickRegresarModicandoUseCase = SetClickRegresarModicandoUseCase(
        setSpanPalabraUseCase,
        setColocarEtiquetasUseCase,
        setPintarTextosUseCase
    )

    @Test
    fun mensaje_de_ya_no_tienes_preguntas_anteriores() {
        val preguntas = arrayListOf("a", "b") // 0, 1
        val respuestas = arrayListOf("a", "b")
        val contador = 0 // 2
        val editable = SpannableStringBuilder("b")
        val isEtPregunta = false

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )
        assertEquals(
            ValidacionesGuiaModel(
                message = "Ya no tienes preguntas anteriores",
            ), respuesta
        )
        assertEquals(arrayListOf("a", "b"), preguntas)
        assertEquals(arrayListOf("a", "b"), respuestas)
    }

    @Test
    fun mensaje_para_llenar_preguntas_y_respuestas_estando_en_respuesta() {
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b")
        val contador = 2
        val editable = SpannableStringBuilder()
        val isEtPregunta = false

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )
        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta",
            ), respuesta
        )

        assertEquals(arrayListOf("a", "b", "c"), preguntas)
        assertEquals(arrayListOf("a", "b"), respuestas)
    }

    /*@Test
    fun mensaje_para_llenar_pregunta_estando_en_pregunta_con_posicion_valida() {
        val preguntas = arrayListOf("a", "b", "c") // posPregFin = 2
        val respuestas = arrayListOf("a", "b", "c")
        val contador = 2 // <= posPregFin
        val editable = SpannableStringBuilder() // está vacío
        val isEtPregunta = true

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )

        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta",
            ), respuesta
        )
    }*/

    @Test
    fun mensaje_para_llenar_preguntas_y_respuestas_estando_en_pregunta() {
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b", "c")
        val contador = 2
        val editable = SpannableStringBuilder()
        val isEtPregunta = true

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )
        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta",
            ), respuesta
        )

        assertEquals(arrayListOf("a", "b", "c"), preguntas)
        assertEquals(arrayListOf("a", "b", "c"), respuestas)
    }

    @Test
    fun muestra_mensaje_si_esta_llena_la_pregunta_y_no_la_respuesta() {
        // Preparar los datos
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b", "c")
        val contador = 3 // > posPregFin (2)
        val editable = SpannableStringBuilder("algo") // contenido no vacío
        val isEtPregunta = true

        // Configurar los mocks para que devuelvan lo esperado
        every { setSpanPalabraUseCase.invoke(editable) } returns SpanPalabraModel(
            editable = SpannableStringBuilder("algo"),
            message = "",
        )
        every { setColocarEtiquetasUseCase.invoke(editable) } returns SpannableStringBuilder("algo")
        every {
            setPintarTextosUseCase.invoke(
                isEtPregunta = true,
                preguntas,
                respuestas,
                contador
            )
        } returns             ValidacionesGuiaModel(
            estadoUI = EstadoUI(
                isUpdatedAskAns = true,
                isShowQuitColor = true,
                isShowSelColor = true,
            ),
            builder = SpannableStringBuilder("c"),
        )

        // Ejecutar el método con los parámetros necesarios
        val resultado = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )

        // Asegurar que el valor retornado es el esperado
        assertEquals(
            ValidacionesGuiaModel(
                message = "Asegurate de llenar pregunta y respuesta",
            ), resultado
        )
    }

    @Test
    fun solo_recupera_la_pregunta_anterior() {
        val preguntas = arrayListOf("a", "b", "c") // pos = 0,1,2
        val respuestas = arrayListOf("a", "b", "c")
        val contador = 3 // pos = 3
        val editable = SpannableStringBuilder()
        val isEtPregunta = true

        every {
            setPintarTextosUseCase.invoke(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("c"),
                )

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )

        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("c"),
            ), respuesta
        )
        assertEquals(arrayListOf("a", "b", "c"), preguntas)
        assertEquals(arrayListOf("a", "b", "c"), respuestas)
    }

    @Test
    fun solo_recupera_la_pregunta_anterior1() {
        val preguntas = arrayListOf("a", "b", "c") // pos = 0,1,2
        val respuestas = arrayListOf("a", "b", "c")
        val contador = 1 // pos = 3
        val editable = SpannableStringBuilder("b")
        val isEtPregunta = true

        every { setSpanPalabraUseCase.invoke(editable) } returns SpanPalabraModel(
            editable = SpannableStringBuilder("b"),
            message = "",
        )
        every { setColocarEtiquetasUseCase.invoke(editable) } returns SpannableStringBuilder("b")
        every {
            setPintarTextosUseCase.invoke(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("a"),
                )

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )

        assertEquals(
            ValidacionesGuiaModel(
                responseSpanPalabra = SpanPalabraModel(editable = SpannableStringBuilder("b")),
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("a"),
            ), respuesta
        )
        assertEquals(arrayListOf("a", "b", "c"), preguntas)
        assertEquals(arrayListOf("a", "b", "c"), respuestas)
    }

    @Test
    fun guardar_lo_que_se_tiene_en_pregunta_y_pintar_lo_anterior() {
        val preguntas = arrayListOf("a", "b", "c") // pos = 0,1,2
        val respuestas = arrayListOf("a", "b", "c")
        val contador = 2 // pos = 3
        val editable = SpannableStringBuilder("b")
        val isEtPregunta = true

        every { setSpanPalabraUseCase.invoke(editable) } returns SpanPalabraModel(
            editable = SpannableStringBuilder("b"),
            message = "",
        )
        every { setColocarEtiquetasUseCase.invoke(editable) } returns SpannableStringBuilder("b")
        every {
            setPintarTextosUseCase.invoke(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("b"),
                )

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )

        assertEquals(
            ValidacionesGuiaModel(
                responseSpanPalabra = SpanPalabraModel(editable = SpannableStringBuilder("b")),
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("b"),
            ), respuesta
        )
        assertEquals(arrayListOf("a", "b", "b"), preguntas)
        assertEquals(arrayListOf("a", "b", "c"), respuestas)
    }

    @Test
    fun guardar_lo_que_se_tiene_en_respuesta_y_pintar_lo_anterior() {
        val preguntas = arrayListOf("a", "b", "c") // pos = 0,1,2
        val respuestas = arrayListOf("a", "b", "c")
        val contador = 2 // pos = 3
        val editable = SpannableStringBuilder("z")
        val isEtPregunta = false

        every { setSpanPalabraUseCase.invoke(editable) } returns SpanPalabraModel(
            editable = SpannableStringBuilder("z"),
            message = "",
        )
        every { setColocarEtiquetasUseCase.invoke(editable) } returns SpannableStringBuilder("z")
        every {
            setPintarTextosUseCase.invoke(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("b"),
                )

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )

        assertEquals(
            ValidacionesGuiaModel(
                responseSpanPalabra = SpanPalabraModel(editable = SpannableStringBuilder("z")),
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("b"),
            ), respuesta
        )
        assertEquals(arrayListOf("a", "b", "c"), preguntas)
        assertEquals(arrayListOf("a", "b", "z"), respuestas)
    }

    @Test
    fun guardar_lo_que_se_tiene_en_respuesta_y_pintar_lo_anterior2() {
        val preguntas = arrayListOf("a", "b", "c") // pos = 0,1,2
        val respuestas = arrayListOf("a", "b")
        val contador = 2 // pos = 3
        val editable = SpannableStringBuilder("z")
        val isEtPregunta = false

        every { setSpanPalabraUseCase.invoke(editable) } returns SpanPalabraModel(
            editable = SpannableStringBuilder("z"),
            message = "",
        )
        every { setColocarEtiquetasUseCase.invoke(editable) } returns SpannableStringBuilder("z")
        every {
            setPintarTextosUseCase.invoke(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns
                ValidacionesGuiaModel(
                    estadoUI = EstadoUI(
                        isUpdatedAskAns = true,
                        isShowQuitColor = true,
                        isShowSelColor = true,
                    ),
                    builder = SpannableStringBuilder("b"),
                )

        val respuesta = setClickRegresarModicandoUseCase(
            preguntas,
            respuestas,
            contador,
            editable,
            isEtPregunta
        )

        assertEquals(
            ValidacionesGuiaModel(
                responseSpanPalabra = SpanPalabraModel(editable = SpannableStringBuilder("z")),
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                    isThereMoreAsks = true
                ),
                builder = SpannableStringBuilder("b"),
            ), respuesta
        )
        assertEquals(arrayListOf("a", "b", "c"), preguntas)
        assertEquals(arrayListOf("a", "b", "z"), respuestas)
    }
}