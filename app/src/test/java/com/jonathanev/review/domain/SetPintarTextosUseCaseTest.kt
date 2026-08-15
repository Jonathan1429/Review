package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.service.ColorRangeParser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SetPintarTextosUseCaseTest {
    private val colorRangeParser = mockk<ColorRangeParser>(relaxed = true)
    private val setPintarTextosUseCase = SetPintarTextosUseCase(colorRangeParser)
    private val ruta: String = "fake/path"

    @Test
    fun to_processing_an_image() = runTest {
        val result =
            setPintarTextosUseCase.invoke(QuestionContentDomain.Image(ruta, "1.png"))

        assertEquals(QuestionContentDomain.Image(ruta, "1.png"), result)
    }

    @Test
    fun to_processing_a_text() = runTest {
        val textWithTags = "tex«-1»to«-1» de prueba"
        val textWithoutTags = "texto de prueba"
        val list = listOf(ColorRangeDomain(3, 5, -1))
        every { colorRangeParser.invoke(textWithTags) } returns QuestionContentDomain.Text(
            textWithoutTags,
            list
        )

        val response = setPintarTextosUseCase.invoke(
            QuestionContentDomain.Text(textWithTags, emptyList())
        )

        verify { colorRangeParser.invoke(textWithTags) }
        assertEquals(QuestionContentDomain.Text(textWithoutTags, list), response)
    }
}