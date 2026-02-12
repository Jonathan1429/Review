package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
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
            relativeGuidePath,
            relativeGuidePath
        )

        moveGuideUseCase = MoveGuideUseCase(
            guiaRepository,
            directoryManager
        )
    }

    @Test
    fun return_error_path_guide() {
        every {
            directoryManager.createPathGuide(
                relativeGuidePath,
                context.guide.nameGuide
            )
        } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            contextMoving = context,
            relativeGuidePath = relativeGuidePath
        )

        verify { directoryManager.createPathGuide(relativeGuidePath, context.guide.nameGuide) }
        assertEquals(MoveGuideResponse.ErrorPathGuide, response)
    }

    @Test
    fun return_error_moving_guide() {
        val localContext = GuideContext.Moving(
            GuideDomainModel(GuideVersion.V1, "Archivo", ""),
            RelativeGuidePath("path/old/guide"),
            RelativeGuidePath("path/old/guide")
        )

        every { guiaRepository.moveGuide(localContext) } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            contextMoving = localContext,
            relativeGuidePath = relativeGuidePath
        )

        verify { guiaRepository.moveGuide(localContext) }
        assertEquals(MoveGuideResponse.ErrorMovingGuide, response)
    }

    @Test
    fun return_error_path_images() {
        every {
            directoryManager.createPathGuide(
                relativeGuidePath,
                context.guide.nameGuide
            )
        } returns true
        every { guiaRepository.moveGuide(context) } returns true
        every {
            directoryManager.createPathImages(
                guideDomainModel = context.guide,
                isNewFile = true,
                relativePath = relativeGuidePath
            )
        } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            contextMoving = context,
            relativeGuidePath = relativeGuidePath
        )

        verify { directoryManager.createPathGuide(relativeGuidePath, context.guide.nameGuide) }
        verify { guiaRepository.moveGuide(context) }
        verify {
            directoryManager.createPathImages(
                guideResult.guideDomainModel,
                true,
                relativeGuidePath
            )
        }
        assertEquals(MoveGuideResponse.ErrorPathImages, response)
    }

    @Test
    fun return_error_moving_images() {
        val localContext = GuideContext.Moving(
            GuideDomainModel(GuideVersion.V1, "Archivo", ""),
            RelativeGuidePath("path/old/guide"),
            RelativeGuidePath("path/old/guide")
        )

        every { guiaRepository.moveGuide(localContext) } returns true
        every {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = localContext.guide,
                imageSource = ImageSource.MovingGuide(
                    localContext.oldRelativeGuidePath,
                    localContext.relativeGuidePath
                )
            )
        } returns false

        every { directoryManager.deleteFolderEmpty(localContext) } just Runs

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            contextMoving = localContext,
            relativeGuidePath = relativeGuidePath
        )

        verify { guiaRepository.moveGuide(localContext) }
        verify {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = localContext.guide,
                imageSource = ImageSource.MovingGuide(
                    localContext.oldRelativeGuidePath,
                    localContext.relativeGuidePath
                )
            )
        }
        verify { directoryManager.deleteFolderEmpty(localContext) }
        assertEquals(MoveGuideResponse.ErrorMovingImages, response)
    }

    @Test
    fun move_guide_successful() {
        every {
            directoryManager.createPathGuide(
                relativeGuidePath,
                context.guide.nameGuide
            )
        } returns true
        every { guiaRepository.moveGuide(context) } returns true
        every {
            directoryManager.createPathImages(
                guideDomainModel = context.guide,
                isNewFile = true,
                relativePath = relativeGuidePath
            )
        } returns true
        every {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = context.guide,
                imageSource = ImageSource.MovingGuide(
                    context.oldRelativeGuidePath,
                    context.relativeGuidePath
                )
            )
        } returns true

        every { directoryManager.deleteFolderEmpty(context) } just Runs

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            contextMoving = context,
            relativeGuidePath = relativeGuidePath
        )

        verify { directoryManager.createPathGuide(relativeGuidePath, context.guide.nameGuide) }
        verify { guiaRepository.moveGuide(context) }
        verify {
            directoryManager.createPathImages(
                guideDomainModel = guideResult.guideDomainModel,
                isNewFile = true,
                relativePath = relativeGuidePath
            )
        }
        verify {
            directoryManager.moveImages(
                images = any(),
                guideDomainModel = context.guide,
                imageSource = ImageSource.MovingGuide(
                    context.oldRelativeGuidePath,
                    context.relativeGuidePath
                )
            )
        }
        verify { directoryManager.deleteFolderEmpty(context) }
        assertEquals(MoveGuideResponse.Success, response)
    }
}