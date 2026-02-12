package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGuideXmlDataUseCaseTest {
    @MockK
    lateinit var guiaRepository: GuiaRepository

    private lateinit var getGuideXmlDataUseCase: GetGuideXmlDataUseCase
    private lateinit var guideDomainModel: GuideDomainModel
    private var relativeGuidePath = RelativeGuidePath("init")

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        guideDomainModel = GuideDomainModel(GuideVersion.V2, "Guia de prueba", "")
        relativeGuidePath = RelativeGuidePath("path/fake")

        getGuideXmlDataUseCase = GetGuideXmlDataUseCase(guiaRepository)
    }

    @Test
    fun error_guide_not_found() {
        val context = GuideContext.DeleteGuide(guideDomainModel, relativeGuidePath)

        val response = getGuideXmlDataUseCase.invoke(context)

        assertEquals(GetGuideResult.NotFound, response)
    }

    @Test
    fun search_for_a_guide_correctly_with_context_browsing() {
        val context = GuideContext.Browsing(guideDomainModel, relativeGuidePath)

        every {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        } returns GetGuideResult.Success(guideDomainModel, emptyList())

        val response = getGuideXmlDataUseCase.invoke(context)

        verify {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        }

        assertEquals(GetGuideResult.Success(guideDomainModel, emptyList()), response)
    }

    @Test
    fun search_for_a_guide_correctly_with_context_editing() {
        val context = GuideContext.Editing(guideDomainModel, relativeGuidePath)

        every {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        } returns GetGuideResult.Success(guideDomainModel, emptyList())

        val response = getGuideXmlDataUseCase.invoke(context)

        verify {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        }

        assertEquals(GetGuideResult.Success(guideDomainModel, emptyList()), response)
    }

    @Test
    fun search_for_a_guide_correctly_with_context_moving() {
        val context = GuideContext.Moving(guideDomainModel, relativeGuidePath, relativeGuidePath)

        every {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        } returns GetGuideResult.Success(guideDomainModel, emptyList())

        val response = getGuideXmlDataUseCase.invoke(context)

        verify {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        }

        assertEquals(GetGuideResult.Success(guideDomainModel, emptyList()), response)
    }
}