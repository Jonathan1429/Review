package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import kotlin.test.Ignore

class GetPreviewQuestionsUseCaseTest {
    @MockK
    lateinit var setPintarTextosUseCase: SetPintarTextosUseCase
    private lateinit var useCase: GetPreviewQuestionsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetPreviewQuestionsUseCase(setPintarTextosUseCase)
    }

    @Ignore("No")
    fun `should take first text from question and count images from question and answer`() =
        runTest {

        // -------- Given --------

        val questionItem1 = QuestionContentDomain.Text("Primer texto", emptyList())
        val questionItem2 = QuestionContentDomain.Image("uri", "img.png")
        val answerItem1 = QuestionContentDomain.Image("uri2", "img2.png")
        val answerItem2 = QuestionContentDomain.Text("Segundo texto", emptyList())

        // Configuramos qué devuelve el use case para cada item
            coEvery {
                setPintarTextosUseCase.invoke(questionItem1)
        } returns questionItem1

            coEvery {
                setPintarTextosUseCase.invoke(questionItem2)
        } returns questionItem2

            coEvery {
                setPintarTextosUseCase.invoke(answerItem1)
        } returns answerItem1

            coEvery {
                setPintarTextosUseCase.invoke(answerItem2)
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

            val result = useCase.invoke(listOf(qaItem1, qaItem2))

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