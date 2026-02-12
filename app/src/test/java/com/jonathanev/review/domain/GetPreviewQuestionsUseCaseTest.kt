package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPreviewQuestionsUseCaseTest {
    @MockK
    lateinit var setPintarTextosUseCase: SetPintarTextosUseCase

    private lateinit var useCase: GetPreviewQuestionsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetPreviewQuestionsUseCase(setPintarTextosUseCase)
    }

    @Test
    fun `should take first text from question and count images from question and answer`() {

        // -------- Given --------

        val questionItem1 = QuestionContentDomain.Text("Primer texto", emptyList())
        val questionItem2 = QuestionContentDomain.Image("uri", "img.png")
        val answerItem1 = QuestionContentDomain.Image("uri2", "img2.png")
        val answerItem2 = QuestionContentDomain.Text("Segundo texto", emptyList())

        // Configuramos qué devuelve el use case para cada item
        every {
            setPintarTextosUseCase.invoke(questionItem1, any())
        } returns questionItem1

        every {
            setPintarTextosUseCase.invoke(questionItem2, any())
        } returns questionItem2

        every {
            setPintarTextosUseCase.invoke(answerItem1, any())
        } returns answerItem1

        every {
            setPintarTextosUseCase.invoke(answerItem2, any())
        } returns answerItem2

        val qaItem1 = QAItemDomain(
            question = QuestionItemDomain(listOf(questionItem1, questionItem2, questionItem1)),
            answer = QuestionItemDomain(listOf(answerItem1, answerItem2, answerItem1))
        )

        val qaItem2 = QAItemDomain(
            question = QuestionItemDomain(listOf(questionItem2)),
            answer = QuestionItemDomain(listOf(answerItem1))
        )

        val relativePath = RelativeGuidePath("path")

        // -------- When --------

        val result = useCase.invoke(listOf(qaItem1, qaItem2), relativePath)

        // -------- Then --------

        assertEquals(2, result.size)

        val previewOne = result.first()
        val previewTwo = result[1]

        assertTrue(previewOne.question is QuestionContentDomain.Text)
        assertEquals(
            "Primer texto",
            (previewOne.question as QuestionContentDomain.Text).text
        )

        // 1 imagen en pregunta + 1 en respuesta
        assertEquals(3, previewOne.noImages)
        assertEquals(2, previewTwo.noImages)
    }
}