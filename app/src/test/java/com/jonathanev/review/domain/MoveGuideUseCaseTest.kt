package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.MoveGuideResponse
import io.mockk.every
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

    @Before
    fun setUp() {
        val guideDomain = GuideDomainModel(
            version = GuideVersion.V2,
            nameGuide = "Archivo 1",
            description = "Sin Descripcion"
        )
        val list = listOf(
            QAItemDomain(
                question = ResponseDomain.Filled(
                    QuestionItemDomain(
                        listOf(
                            QuestionContentDomain.Text("Texto", emptyList())
                        )
                    )
                ),
                answer = ResponseDomain.Filled(
                    QuestionItemDomain(
                        listOf(
                            QuestionContentDomain.Text("Texto2", emptyList())
                        )
                    )
                )
            )
        )

        guideResult = GetGuideResult.Success(guideDomain, list)
        context = GuideContext.Moving(
            guideDomain,
            GuidePath("path/old/guide"),
            GuidePath("path/old/image")
        )

        moveGuideUseCase = MoveGuideUseCase(
            guiaRepository,
            directoryManager
        )
    }

    @Test
    fun return_error_path_guide() {
        every { directoryManager.createPathGuide(context.guide.nameGuide) } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = context
        )

        verify { directoryManager.createPathGuide(context.guide.nameGuide) }
        assertEquals(MoveGuideResponse.ErrorPathGuide, response)
    }

    @Test
    fun return_error_moving_guide() {
        val localContext = GuideContext.Moving(
            GuideDomainModel(GuideVersion.V1, "Archivo", ""),
            GuidePath("path/old/guide"),
            GuidePath("path/old/image")
        )

        every { guiaRepository.moveGuide(localContext) } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = localContext
        )

        verify { guiaRepository.moveGuide(localContext) }
        assertEquals(MoveGuideResponse.ErrorMovingGuide, response)
    }

    @Test
    fun return_warning_delete_folder() {
        every { directoryManager.createPathGuide(context.guide.nameGuide) } returns true
        every { guiaRepository.moveGuide(context) } returns true
        every { directoryManager.deleteFolderEmpty(context) } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = context
        )

        verify { directoryManager.createPathGuide(context.guide.nameGuide) }
        verify { guiaRepository.moveGuide(context) }
        verify { directoryManager.deleteFolderEmpty(context) }
        assertEquals(MoveGuideResponse.WarningDeleteFolder, response)
    }

    @Test
    fun return_error_path_images() {
        every { directoryManager.createPathGuide(context.guide.nameGuide) } returns true
        every { guiaRepository.moveGuide(context) } returns true
        every { directoryManager.deleteFolderEmpty(context) } returns true
        every {
            directoryManager.createPathImages(
                guideDomainModel = context.guide,
                isNewFile = true
            )
        } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = context
        )

        verify { directoryManager.createPathGuide(context.guide.nameGuide) }
        verify { guiaRepository.moveGuide(context) }
        verify { directoryManager.deleteFolderEmpty(context) }
        verify { directoryManager.createPathImages(guideResult.guideDomainModel, true) }
        assertEquals(MoveGuideResponse.ErrorPathImages, response)
    }

    @Test
    fun return_error_moving_images() {
        val localContext = GuideContext.Moving(
            GuideDomainModel(GuideVersion.V1, "Archivo", ""),
            GuidePath("path/old/guide"),
            GuidePath("path/old/image")
        )

        every { guiaRepository.moveGuide(localContext) } returns true
        every { directoryManager.deleteFolderEmpty(localContext) } returns true
        every {
            directoryManager.moveImages(
                guideDomain = localContext.guide,
                imageSource = ImageSource.MovingGuide(GuidePath(localContext.oldImagePath.value)),
                images = any()
            )
        } returns false

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = localContext
        )

        verify { guiaRepository.moveGuide(localContext) }
        verify { directoryManager.deleteFolderEmpty(localContext) }
        verify {
            directoryManager.moveImages(
                guideDomain = localContext.guide,
                imageSource = ImageSource.MovingGuide(GuidePath(localContext.oldImagePath.value)),
                images = any()
            )
        }
        assertEquals(MoveGuideResponse.ErrorMovingImages, response)
    }

    @Test
    fun move_guide_successful() {
        every { directoryManager.createPathGuide(context.guide.nameGuide) } returns true
        every { guiaRepository.moveGuide(context) } returns true
        every { directoryManager.deleteFolderEmpty(context) } returns true
        every {
            directoryManager.createPathImages(
                guideDomainModel = context.guide,
                isNewFile = true
            )
        } returns true
        every {
            directoryManager.moveImages(
                guideDomain = context.guide,
                imageSource = ImageSource.MovingGuide(GuidePath(context.oldImagePath.value)),
                images = any()
            )
        } returns true

        val response = moveGuideUseCase.invoke(
            guideData = guideResult,
            context = context
        )

        verify { directoryManager.createPathGuide(context.guide.nameGuide) }
        verify { guiaRepository.moveGuide(context) }
        verify { directoryManager.deleteFolderEmpty(context) }
        verify {
            directoryManager.createPathImages(
                guideDomainModel = guideResult.guideDomainModel,
                isNewFile = true
            )
        }
        verify {
            directoryManager.moveImages(
                guideDomain = context.guide,
                imageSource = ImageSource.MovingGuide(GuidePath(context.oldImagePath.value)),
                images = any()
            )
        }
        assertEquals(MoveGuideResponse.Success, response)
    }
}