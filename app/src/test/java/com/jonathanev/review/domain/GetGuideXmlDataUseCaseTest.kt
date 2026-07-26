package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.GetGuideResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGuideXmlDataUseCaseTest {
    @MockK
    lateinit var guiaRepository: GuiaRepository
    private var navigationPathRepository = mockk<NavigationPathRepository>()
    private lateinit var getGuideXmlDataUseCase: GetGuideXmlDataUseCase
    private lateinit var guideDomainModel: GuideDomainModel
    private var relativeGuidePath = RelativeGuidePath("init")

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        guideDomainModel = GuideDomainModel(GuideVersion.V2, "Guia de prueba", "")
        relativeGuidePath = RelativeGuidePath("path/fake")

        getGuideXmlDataUseCase = GetGuideXmlDataUseCase(guiaRepository, navigationPathRepository)
    }

    @Test
    fun error_guide_not_found() = runTest {
        val context = GuideContext.DeleteGuide(guideDomainModel, relativeGuidePath)

        val response = getGuideXmlDataUseCase.invoke(context)

        assertEquals(GetGuideResult.NotFound, response)
    }

    @Test
    fun search_for_a_guide_correctly_with_context_browsing() = runTest {
        val context = GuideContext.Browsing(guideDomainModel)

        coEvery {
            guiaRepository.getXMLGuide(guideDomainModel)
        } returns GetGuideResult.Success(guideDomainModel, emptyList())

        val response = getGuideXmlDataUseCase.invoke(context)

        coVerify {
            guiaRepository.getXMLGuide(guideDomainModel)
        }

        assertEquals(GetGuideResult.Success(guideDomainModel, emptyList()), response)
    }

    @Test
    fun search_for_a_guide_correctly_with_context_editing() = runTest {
        val context = GuideContext.Editing(guideDomainModel)

        coEvery {
            guiaRepository.getXMLGuide(guideDomainModel)
        } returns GetGuideResult.Success(guideDomainModel, emptyList())

        val response = getGuideXmlDataUseCase.invoke(context)

        coVerify {
            guiaRepository.getXMLGuide(guideDomainModel)
        }

        assertEquals(GetGuideResult.Success(guideDomainModel, emptyList()), response)
    }

    @Test
    fun search_for_a_guide_correctly_with_context_moving() = runTest {
        val context = GuideContext.Moving(guideDomainModel, relativeGuidePath, relativeGuidePath)

        coEvery {
            guiaRepository.getXMLGuide(guideDomainModel)
        } returns GetGuideResult.Success(guideDomainModel, emptyList())

        val response = getGuideXmlDataUseCase.invoke(context)

        coVerify {
            guiaRepository.getXMLGuide(guideDomainModel)
        }

        assertEquals(GetGuideResult.Success(guideDomainModel, emptyList()), response)
    }
}