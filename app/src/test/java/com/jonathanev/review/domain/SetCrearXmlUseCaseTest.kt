package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class SetCrearXmlUseCaseTest {
    private val guiaRepository = mockk<GuiaRepository>()
    private val directoryManager = mockk<DirectoryManager>()
    private val setCrearXmlUseCase = SetCrearXmlUseCase(guiaRepository, directoryManager)

    @Test
    fun error_creating_guide_route() {
        every { directoryManager.createPathGuide("Prueba") } returns false

        val item =
            listOf(QuestionItemDomain(listOf(QuestionContentDomain.Text("Texto 1", emptyList()))))

        val response = setCrearXmlUseCase.invoke(
            nameGuide = "Prueba",
            description = "Sin descripcion",
            version = GuideVersion.V2,
            preguntas = item,
            respuestas = item,
            relativeGuidePath = RelativeGuidePath("relativeGuidePath")
        )

        verify { directoryManager.createPathGuide("Prueba") }
        Assert.assertFalse(response)
    }

    @Test
    fun error_creating_guide() {
        val item =
            listOf(QuestionItemDomain(listOf(QuestionContentDomain.Text("Texto 1", emptyList()))))

        every { directoryManager.createPathGuide("Prueba") } returns true
        every {
            guiaRepository.saveGuide(
                guideDomainModel = GuideDomainModel(
                    GuideVersion.V1,
                    "Prueba",
                    "Sin descripcion"
                ),
                preguntas = item,
                respuestas = item,
                relativeGuidePath = RelativeGuidePath("relativeGuidePath")
            )
        } returns false

        val response = setCrearXmlUseCase.invoke(
            nameGuide = "Prueba",
            description = "Sin descripcion",
            version = GuideVersion.V1,
            preguntas = item,
            respuestas = item,
            relativeGuidePath = RelativeGuidePath("relativeGuidePath")
        )

        verify { directoryManager.createPathGuide("Prueba") }

        verify { guiaRepository.saveGuide(
            guideDomainModel = GuideDomainModel(
                GuideVersion.V1,
                "Prueba",
                "Sin descripcion"
            ),
            preguntas = item,
            respuestas = item,
            relativeGuidePath = RelativeGuidePath("relativeGuidePath")
        ) }

        Assert.assertFalse(response)
    }

    @Test
    fun success_creating_guide() {
        val item =
            listOf(QuestionItemDomain(listOf(QuestionContentDomain.Text("Texto 1", emptyList()))))

        every { directoryManager.createPathGuide("Prueba") } returns true
        every {
            guiaRepository.saveGuide(
                guideDomainModel = GuideDomainModel(
                    GuideVersion.V2,
                    "Prueba",
                    "Sin descripcion"
                ),
                preguntas = item,
                respuestas = item,
                relativeGuidePath = RelativeGuidePath("relativeGuidePath")
            )
        } returns true

        val response = setCrearXmlUseCase.invoke(
            nameGuide = "Prueba",
            description = "Sin descripcion",
            version = GuideVersion.V2,
            preguntas = item,
            respuestas = item,
            relativeGuidePath = RelativeGuidePath("relativeGuidePath")
        )

        verify { directoryManager.createPathGuide("Prueba") }
        verify {
            guiaRepository.saveGuide(
                guideDomainModel = GuideDomainModel(
                    GuideVersion.V2,
                    "Prueba",
                    "Sin descripcion"
                ),
                preguntas = item,
                respuestas = item,
                relativeGuidePath = RelativeGuidePath("relativeGuidePath")
            )
        }
        Assert.assertTrue(response)
    }
}