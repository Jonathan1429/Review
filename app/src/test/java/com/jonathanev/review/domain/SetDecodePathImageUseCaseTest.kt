package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.UserPreferencesRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SetDecodePathImageUseCaseTest {
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()
    private val setDecodePathImageUseCase = SetDecodePathImageUseCase(userPreferencesRepository)

    @Test
    fun assigning_nameFile_when_its_an_image() = runTest {
        val fakeUri = "fake/uri"
        val listContent = listOf(
            QuestionContentDomain.Image(fakeUri, "")
        )
        val newListContentQ = listOf(
            QuestionContentDomain.Image(fakeUri, "2.png")
        )
        val newListContentA = listOf(
            QuestionContentDomain.Image(fakeUri, "3.png")
        )
        val preguntas = QuestionItemDomain(listContent)
        val respuestas = QuestionItemDomain(listContent)

        every { userPreferencesRepository.getCountImage() } returns flowOf(1)
        coEvery { userPreferencesRepository.setImageCount(any()) } just Runs

        val response = setDecodePathImageUseCase.invoke(listOf(preguntas), listOf(respuestas))

        coVerify { userPreferencesRepository.getCountImage() }
        coVerify { userPreferencesRepository.setImageCount(any()) }

        assertEquals(
            Pair(
                listOf(QuestionItemDomain(newListContentQ)),
                listOf(QuestionItemDomain(newListContentA))
            ),
            response
        )
    }

    @Test
    fun rules_that_are_not_followed_do_not_change_the_nameFile() = runTest {
        val listContentQ = listOf(
            QuestionContentDomain.Text("hola", emptyList()),
            QuestionContentDomain.Image("adios", "1.png"),
        )
        val listContentA = listOf(
            QuestionContentDomain.Text("hola", emptyList()),
            QuestionContentDomain.Image("adios", "2.png"),
        )

        every { userPreferencesRepository.getCountImage() } returns flowOf(2)
        coEvery { userPreferencesRepository.setImageCount(2) } just Runs

        val preguntas = QuestionItemDomain(listContentQ)
        val respuestas = QuestionItemDomain(listContentA)
        val response = setDecodePathImageUseCase.invoke(listOf(preguntas), listOf(respuestas))

        coVerify { userPreferencesRepository.setImageCount(2) }

        assertEquals(
            Pair(listOf(QuestionItemDomain(listContentQ)), listOf(QuestionItemDomain(listContentA))),
            response
        )
    }
}