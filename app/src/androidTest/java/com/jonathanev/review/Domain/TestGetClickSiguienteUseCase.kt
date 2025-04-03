package com.jonathanev.review.Domain

import android.text.SpannableStringBuilder
import com.jonathanev.review.Data.Model.EstadoUI
import com.jonathanev.review.Data.Model.ValidacionesGuiaModel
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestGetClickSiguienteUseCase {
    private val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()
    private val getClickSiguienteUseCase = GetClickSiguienteUseCase(setPintarTextosUseCase)

    @Test
    fun das_click_siguiente_sino_hay_mas_te_regresa_un_mensaje_que_no_hay_mas_preguntas() {
        val contadorPregunta = 2
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b", "c")

        val resultado = getClickSiguienteUseCase(contadorPregunta, preguntas, respuestas)
        assertEquals(
            ValidacionesGuiaModel(
                message = "Se acabaron las preguntas, ¿Quieres repetir la guia?",
            ), resultado
        )
    }

    @Test
    fun das_click_siguiente_si_hay_mas_preguntas_te_va_a_regresar_esa() {
        val contadorPregunta = 1 // Pregunta actual = b
        val preguntas = arrayListOf("a", "b", "c")
        val respuestas = arrayListOf("a", "b", "c")
        val setPintarTextosUseCase = mockk<SetPintarTextosUseCase>()

        // Si nosotros ponemos los valores tenemos que
        /*every {
            setPintarTextosUseCase.invoke(
                true,
                preguntas,
                respuestas,
                contadorPregunta + 1, // Pregunta que pintará = c
                true
            )
        }*/

        // Si ponemos hacemos que el mock ponga los valores necesarios para que nos regrese ese reultado
        every {
            setPintarTextosUseCase.invoke(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns ValidacionesGuiaModel(
            estadoUI = EstadoUI(
                isUpdatedAskAns = true,
                isShowQuitColor = true,
                isShowSelColor = true,
            ),
            builder = SpannableStringBuilder(preguntas[2]),
        )
        // Así nos aseguramos que el GetClickRegresarUseCase use el mock
        val getClickSiguienteUseCase = GetClickSiguienteUseCase(setPintarTextosUseCase)
        val resultado = getClickSiguienteUseCase(contadorPregunta, preguntas, respuestas)
        assertEquals(
            ValidacionesGuiaModel(
                estadoUI = EstadoUI(
                    isUpdatedAskAns = true,
                    isShowQuitColor = true,
                    isShowSelColor = true,
                ),
                builder = SpannableStringBuilder(preguntas[2]),
            ), resultado
        )
    }
}