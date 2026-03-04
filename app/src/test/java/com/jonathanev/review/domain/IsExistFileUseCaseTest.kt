package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsExistFileUseCaseTest {
    private lateinit var cachedGuides: List<GuideDomainModel>
    private lateinit var isExistFileUseCase: IsExistFileUseCase

    @Before
    fun setUp() {
        cachedGuides = listOf(
            GuideDomainModel(GuideVersion.V2, "Abap", ""),
            GuideDomainModel(GuideVersion.V2, "Kotlin", "")
        )

        isExistFileUseCase = IsExistFileUseCase()
    }

    @Test
    fun the_guide_does_not_exist(){
        val response = isExistFileUseCase.invoke(cachedGuides, "Testing", mode.fileName)

        assertFalse(response)
    }

    @Test
    fun the_guide_exists(){
        val response = isExistFileUseCase.invoke(cachedGuides, "Kotlin", mode.fileName)

        assertTrue(response)
    }
}