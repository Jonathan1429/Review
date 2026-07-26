package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsExistFileUseCaseTest {
    private lateinit var cachedGuides: List<GuideDomainModel>
    private lateinit var isExistFileUseCase: IsExistFileUseCase
    private var guiaRepository = mockk<GuiaRepository>()
    private val navigationPathRepository = mockk<NavigationPathRepository>()

    @Before
    fun setUp() {
        cachedGuides = listOf(
            GuideDomainModel(GuideVersion.V2, "Abap", ""),
            GuideDomainModel(GuideVersion.V2, "Kotlin", "")
        )

        isExistFileUseCase = IsExistFileUseCase(guiaRepository, navigationPathRepository)
    }

    @Test
    fun the_guide_does_not_exist() = runTest {
        val response = isExistFileUseCase.invoke("Testing")

        assertFalse(response)
    }

    @Test
    fun the_guide_exists() = runTest {
        val response = isExistFileUseCase.invoke("Kotlin")

        assertTrue(response)
    }
}