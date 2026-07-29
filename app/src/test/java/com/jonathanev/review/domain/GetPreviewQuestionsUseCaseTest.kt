package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `should take first text from question and count images from question and answer`() =
        runTest {
            // -------- Given --------
            val questionItem1 = QuestionContentDomain.Text("Primer texto", emptyList())
            val questionItem2 = QuestionContentDomain.Image("uri", "img.png")
            val answerItem1 = QuestionContentDomain.Text("Segundo texto", emptyList())
            val answerItem2 = QuestionContentDomain.Image("uri2", "img2.png")

            // Configuramos qué devuelve el use case para cada item
            coEvery {
                setPintarTextosUseCase.invoke(questionItem1)
            } returns questionItem1

            coEvery {
                setPintarTextosUseCase.invoke(questionItem2)
            } returns questionItem2

            coEvery {
                setPintarTextosUseCase.invoke(answerItem2)
            } returns answerItem2

            coEvery {
                setPintarTextosUseCase.invoke(answerItem1)
            } returns answerItem1

            val qaItem1 = QAItemDomain(
                question = QuestionItemDomain(listOf(questionItem1, questionItem2, questionItem1)),
                answer = QuestionItemDomain(listOf(answerItem2, answerItem1))
            )

            val qaItem2 = QAItemDomain(
                question = QuestionItemDomain(listOf(questionItem2)),
                answer = QuestionItemDomain(listOf(answerItem2))
            )

            // -------- When --------
            val result = useCase.invoke(listOf(qaItem1, qaItem2))

            // -------- Then --------
            assertEquals(2, result.size)

            val previewOne = result.first()
            val previewTwo = result[1]

            assertEquals(
                /* expected = */ "Primer texto",
                /* actual = */ previewOne.question.text
            )

            assertEquals(3, previewOne.noTexts)
            assertEquals(2, previewOne.noImages)

            assertEquals(0, previewTwo.noTexts)
            assertEquals(2, previewTwo.noImages)
        }
}