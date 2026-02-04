package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.ColorRangeDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class SetLabelsUseCaseTest {
    private val setLabelsUseCase = SetLabelsUseCase()

    @Test
    fun set_labels_questions() {
        val questions =
            listOf(
                QuestionItemDomain(
                    listOf(
                        QuestionContentDomain.Text("Prueba", listOf(ColorRangeDomain(0, 1, 1)))
                    )
                )
            )

        val newQuestions = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text("«1»P«/1»rueba", listOf(ColorRangeDomain(0, 1, 1)))
                )
            )
        )

        val response = setLabelsUseCase.invoke(questions, emptyList())

        assertEquals(Pair(newQuestions, emptyList<QuestionItemDomain>()), response)
    }

    @Test
    fun return_items_without_change() {
        val questions =
            listOf(
                QuestionItemDomain(
                    listOf(
                        QuestionContentDomain.Image("Uri", "1.png")
                    )
                )
            )

        val response = setLabelsUseCase.invoke(questions, emptyList())

        assertEquals(Pair(questions, emptyList<QuestionItemDomain>()), response)
    }
}