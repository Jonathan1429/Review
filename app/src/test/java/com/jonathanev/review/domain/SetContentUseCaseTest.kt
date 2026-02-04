package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import org.junit.Assert.assertEquals
import org.junit.Test

class SetContentUseCaseTest {
    private val setContentUseCase: SetContentUseCase = SetContentUseCase()

    @Test
    fun counter_out_of_index_returns_intact_list() {
        val newContent = QuestionContentDomain.Text("Nuevo texto", emptyList())
        val sourceList = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text(
                        "Viejo texto",
                        emptyList()
                    )
                )
            )
        )

        val response = setContentUseCase.invoke(
            newContent = newContent,
            sourceList = sourceList,
            contadorPregunta = 1,
            contadorContenido = 0,
            isEditingMode = true,
            filterType = QuestionContentDomain.Text::class.java
        )

        assertEquals(sourceList, response)
    }

    @Test
    fun replace_the_content_of_a_position() {
        val newContent = QuestionContentDomain.Text("Nuevo texto", emptyList())
        val sourceList = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text(
                        "Viejo texto",
                        emptyList()
                    )
                )
            )
        )
        val newSourceList = listOf(
            QuestionItemDomain(
                listOf(
                    newContent
                )
            )
        )

        val response = setContentUseCase.invoke(
            newContent = newContent,
            sourceList = sourceList,
            contadorPregunta = 0,
            contadorContenido = 0,
            isEditingMode = true,
            filterType = QuestionContentDomain.Text::class.java
        )

        assertEquals(newSourceList, response)
    }

    @Test
    fun add_new_content_in_edit_mode() {
        val newContent = QuestionContentDomain.Text("Nuevo texto", emptyList())
        val sourceList = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text(
                        "Viejo texto",
                        emptyList()
                    )
                )
            )
        )
        val newSourceList = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text(
                        "Viejo texto",
                        emptyList()
                    ),
                    newContent
                )
            )
        )

        val response = setContentUseCase.invoke(
            newContent = newContent,
            sourceList = sourceList,
            contadorPregunta = 0,
            contadorContenido = 1,
            isEditingMode = true,
            filterType = QuestionContentDomain.Text::class.java
        )

        assertEquals(newSourceList, response)
    }

    @Test
    fun add_new_content_in_create_mode() {
        val newContent = QuestionContentDomain.Image("uri", "1.png")
        val sourceList = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text(
                        "Viejo texto",
                        emptyList()
                    )
                )
            )
        )
        val newSourceList = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text(
                        "Viejo texto",
                        emptyList()
                    ),
                    newContent
                )
            )
        )

        val response = setContentUseCase.invoke(
            newContent = newContent,
            sourceList = sourceList,
            contadorPregunta = 0,
            contadorContenido = 1,
            isEditingMode = false,
            filterType = QuestionContentDomain.Text::class.java
        )

        assertEquals(newSourceList, response)
    }
}