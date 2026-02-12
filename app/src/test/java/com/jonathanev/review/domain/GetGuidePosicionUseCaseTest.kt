package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.result.GuideResultDomain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGuidePosicionUseCaseTest {
    private lateinit var guides: List<GuideDomainModel>
    private lateinit var getGuidePosicionUseCase: GetGuidePosicionUseCase
    @Before
    fun setUp() {
        guides = listOf(
            GuideDomainModel(GuideVersion.V2, "Abap", ""),
            GuideDomainModel(GuideVersion.V2, "Kotlin", "")
        )

        getGuidePosicionUseCase = GetGuidePosicionUseCase()
    }

    @Test
    fun returns_a_null_value(){
        val response = getGuidePosicionUseCase.invoke(2, guides)

        assertEquals(GuideResultDomain.Error, response)
    }

    @Test
    fun return_a_guide(){
        val response = getGuidePosicionUseCase.invoke(1, guides)

        assertEquals(GuideResultDomain.Success(GuideDomainModel(GuideVersion.V2, "Kotlin", "")), response)
    }
}