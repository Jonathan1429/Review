package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.service.ColorRangeParser
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class SetPintarTextosUseCaseTest {
    private val colorRangeParser = mockk<ColorRangeParser>(relaxed = true)
    private val setPintarTextosUseCase = SetPintarTextosUseCase(colorRangeParser)
    private val ruta: String = "fake/path"

    @Test
    fun to_processing_an_image(){
        val result = setPintarTextosUseCase.invoke(QuestionContentDomain.Image("uri", "1.png"), ruta)

        assertEquals(QuestionContentDomain.Image(ruta, "1.png"), result)
    }

    @Test
    fun to_processing_a_text(){
        val text = "texto de prueba"
        setPintarTextosUseCase.invoke(QuestionContentDomain.Text(text, listOf(ColorRangeDomain(4, 6, -1))), ruta)

        verify(exactly = 1) { colorRangeParser.invoke(text) }
    }

    @Test
    fun to_processing_anything(){
        val result = setPintarTextosUseCase.invoke(QuestionContentDomain.None, ruta)

        assertEquals(QuestionContentDomain.None, result)
    }
}