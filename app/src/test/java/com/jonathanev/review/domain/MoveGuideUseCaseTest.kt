package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageContext
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MoveGuideUseCaseTest {
    private val guiaRepository = mockk<GuiaRepository>()
    private val directoryManager = mockk<DirectoryManager>()
    private lateinit var moveGuideUseCase: MoveGuideUseCase
    private lateinit var guideResult: GetGuideResult.Success
    private lateinit var context: GuideContext.Moving
    private var relativeGuidePath: RelativeGuidePath = RelativeGuidePath("init")

    @Before
    fun setUp() {
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Archivo 1",
            description = "Sin Descripcion"
        )
        val list = listOf(
            QAItemDomain(
                question = QuestionItemDomain(
                    listOf(
                        QuestionContentDomain.Text("Texto", emptyList())
                    )
                ),
                answer = QuestionItemDomain(
                    listOf(
                        QuestionContentDomain.Text("Texto2", emptyList())
                    )
                )
            )
        )

        relativeGuidePath = RelativeGuidePath("path/old/guide")
        guideResult = GetGuideResult.Success(guideDomain, list)
        context = GuideContext.Moving(
            guideDomain,
            relativeGuidePath
        )

        moveGuideUseCase = MoveGuideUseCase(
            guiaRepository,
            directoryManager
        )
    }

    @Test
    fun return_error_path_guide() = runTest {
        coEvery {
            directoryManager.createPathGuide(
                context.guide
            )
        } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = context
        )

        coVerify { directoryManager.createPathGuide(context.guide) }
        assertEquals(MoveGuideResponse.ErrorPathGuide, response)
    }

    @Test
    fun return_error_moving_guide() = runTest {
        val localContext = GuideContext.Moving(
            GuideDomainModel(GuideVersion.V1, "Archivo", ""),
            RelativeGuidePath("path/old/guide")
        )

        coEvery { guiaRepository.moveGuide(localContext) } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = localContext
        )

        coVerify { guiaRepository.moveGuide(localContext) }
        assertEquals(MoveGuideResponse.ErrorMovingGuide, response)
    }

    @Test
    fun return_error_path_images() = runTest {
        coEvery {
            directoryManager.createPathGuide(
                context.guide
            )
        } returns true
        coEvery { guiaRepository.moveGuide(context) } returns true
        coEvery {
            directoryManager.createPathImages(
                guideDomainModel = context.guide,
                isNewFile = true
            )
        } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = context
        )

        coVerify { directoryManager.createPathGuide(context.guide) }
        coVerify { guiaRepository.moveGuide(context) }
        coVerify {
            directoryManager.createPathImages(
                guideResult.guideDomainModel,
                true
            )
        }
        assertEquals(MoveGuideResponse.ErrorPathImages, response)
    }

    @Test
    fun return_error_moving_images() = runTest {
        val localContext = GuideContext.Moving(
            GuideDomainModel(GuideVersion.V1, "Archivo", ""),
            RelativeGuidePath("path/old/guide")
        )

        coEvery { guiaRepository.moveGuide(localContext) } returns true
        coEvery {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = localContext.guide,
                imageContext = ImageContext.MovingImage(
                    localContext.oldRelativeGuidePath
                )
            )
        } returns false

        every { directoryManager.deleteFolderEmpty(localContext) } just Runs

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = localContext
        )

        coVerify { guiaRepository.moveGuide(localContext) }
        coVerify {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = localContext.guide,
                imageContext = ImageContext.MovingImage(
                    localContext.oldRelativeGuidePath
                )
            )
        }
        verify { directoryManager.deleteFolderEmpty(localContext) }
        assertEquals(MoveGuideResponse.ErrorMovingImages, response)
    }

    @Test
    fun move_guide_successful() = runTest {
        coEvery {
            directoryManager.createPathGuide(
                context.guide
            )
        } returns true
        coEvery { guiaRepository.moveGuide(context) } returns true
        coEvery {
            directoryManager.createPathImages(
                guideDomainModel = context.guide,
                isNewFile = true
            )
        } returns true
        coEvery {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = context.guide,
                imageContext = ImageContext.MovingImage(
                    context.oldRelativeGuidePath
                )
            )
        } returns true

        every { directoryManager.deleteFolderEmpty(context) } just Runs

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = context
        )

        coVerify { directoryManager.createPathGuide(context.guide) }
        coVerify { guiaRepository.moveGuide(context) }
        coVerify {
            directoryManager.createPathImages(
                guideDomainModel = guideResult.guideDomainModel,
                isNewFile = true
            )
        }
        coVerify {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = context.guide,
                imageContext = ImageContext.MovingImage(
                    context.oldRelativeGuidePath
                )
            )
        }
        verify { directoryManager.deleteFolderEmpty(context) }
        assertEquals(MoveGuideResponse.Success, response)
    }
}