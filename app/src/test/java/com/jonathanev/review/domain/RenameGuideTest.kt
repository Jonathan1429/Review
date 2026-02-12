package com.jonathanev.review.domain

import com.jonathanev.review.domain.mapper.GuideQuestionExtractor
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.OptionalAttrGuide
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.RequiredAttrGuide
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.GetGuideResult
import com.jonathanev.review.domain.result.RenamedGuideResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RenameGuideTest {
    private val guiaRepository = mockk<GuiaRepository>()
    private val guideQuestionExtractor = mockk<GuideQuestionExtractor>()
    private val imagesRepository = mockk<ImagesRepository>()
    private val directoryManager = mockk<DirectoryManager>()
    private lateinit var guideDomain: GuideDomainModel
    private var oldRelativeGuidePath: RelativeGuidePath = RelativeGuidePath("init")
    private lateinit var context: GuideContext.Editing
    private lateinit var result: List<QAItemDomain>
    private lateinit var newName: String
    private lateinit var questionItemDomain: QuestionItemDomain
    private lateinit var answerItemDomain: QuestionItemDomain
    private lateinit var renameGuideUseCase: RenameGuideUseCase

    @Before
    fun setUp() {
        guideDomain = GuideDomainModel(
            GuideVersion.V1,
            "Prueba",
            "Descripcion"
        )
        oldRelativeGuidePath = RelativeGuidePath("fake/path/oldGuide")
        renameGuideUseCase = RenameGuideUseCase(
            guiaRepository = guiaRepository,
            guideQuestionExtractor = guideQuestionExtractor,
            imagesRepository = imagesRepository,
            directoryManager = directoryManager,
        )
        context = GuideContext.Editing(
            guide = guideDomain,
            relativeGuidePath = oldRelativeGuidePath
        )

        questionItemDomain = QuestionItemDomain(
            listOf(
                QuestionContentDomain.Text(
                    "Question 1",
                    emptyList()
                )
            )
        )

        answerItemDomain = QuestionItemDomain(
            listOf(
                QuestionContentDomain.Text(
                    "Answer 1",
                    emptyList()
                )
            )
        )

        result = listOf(
            QAItemDomain(
                question = questionItemDomain,
                answer = answerItemDomain
            )
        )

        newName = "Nuevo archivo"
    }

    @Test
    fun unknownerror_in_get_xml_guide() {
        every {
            guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath)
        } returns GetGuideResult.UnknownError

        renameGuideUseCase.invoke(guideDomain, oldRelativeGuidePath, newName, "")

        verify { guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath) }
    }

    @Test
    fun invalid_format_in_get_xml_guide() {
        every {
            guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath)
        } returns GetGuideResult.InvalidFormat

        renameGuideUseCase.invoke(guideDomain, oldRelativeGuidePath, newName, "Prueba")

        verify { guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath) }
    }

    @Test
    fun not_found_in_get_xml_guide() {
        every {
            guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath)
        } returns GetGuideResult.NotFound

        renameGuideUseCase.invoke(guideDomain, oldRelativeGuidePath, newName, "")

        verify { guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath) }
    }

    @Test
    fun guide_path_error_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.createPathGuide(oldRelativeGuidePath, newName)
        } returns false

        val response =
            renameGuideUseCase.invoke(guideDomain, oldRelativeGuidePath, newName, "")

        verify { guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath) }

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.createPathGuide(oldRelativeGuidePath, newName) }

        assertEquals(RenamedGuideResult.GuidePathError, response)
    }

    @Test
    fun renamed_error_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.createPathGuide(oldRelativeGuidePath, newName)
        } returns true

        every {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guide = guideDomain,
                    relativeGuidePath = oldRelativeGuidePath,
                    name = RequiredAttrGuide(newName),
                    description = OptionalAttrGuide("")
                )
            )
        } returns false

        val response =
            renameGuideUseCase.invoke(guideDomain, oldRelativeGuidePath, newName, "")

        verify { guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath) }

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.createPathGuide(oldRelativeGuidePath, newName) }

        verify {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guide = guideDomain,
                    relativeGuidePath = oldRelativeGuidePath,
                    name = RequiredAttrGuide(newName),
                    description = OptionalAttrGuide("")
                )
            )
        }

        assertEquals(RenamedGuideResult.RenamedError, response)
    }

    @Test
    fun image_error_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.createPathGuide(oldRelativeGuidePath, newName)
        } returns true

        every {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guide = guideDomain,
                    relativeGuidePath = oldRelativeGuidePath,
                    name = RequiredAttrGuide(newName),
                    description = OptionalAttrGuide("")
                )
            )
        } returns true

        every {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName),
                relativeGuidePath = oldRelativeGuidePath
            )
        } returns false

        val response =
            renameGuideUseCase.invoke(guideDomain, oldRelativeGuidePath, newName, "")

        verify { guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath) }

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.createPathGuide(oldRelativeGuidePath, newName) }

        verify {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guide = guideDomain,
                    relativeGuidePath = oldRelativeGuidePath,
                    name = RequiredAttrGuide(newName),
                    description = OptionalAttrGuide("")
                )
            )
        }

        verify {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName),
                relativeGuidePath = oldRelativeGuidePath
            )
        }

        assertEquals(RenamedGuideResult.ImageError, response)
    }

    @Test
    fun success_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.createPathGuide(oldRelativeGuidePath, newName)
        } returns true

        every {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guide = guideDomain,
                    relativeGuidePath = oldRelativeGuidePath,
                    name = RequiredAttrGuide(newName),
                    description = OptionalAttrGuide("")
                )
            )
        } returns true

        every {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName),
                relativeGuidePath = oldRelativeGuidePath
            )
        } returns true

        val response =
            renameGuideUseCase.invoke(guideDomain, oldRelativeGuidePath, newName, "")

        verify { guiaRepository.getXMLGuide(context.guide, context.relativeGuidePath) }

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.createPathGuide(oldRelativeGuidePath, newName) }

        verify {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guide = guideDomain,
                    relativeGuidePath = oldRelativeGuidePath,
                    name = RequiredAttrGuide(newName),
                    description = OptionalAttrGuide("")
                )
            )
        }

        verify {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName),
                relativeGuidePath = oldRelativeGuidePath
            )
        }

        assertEquals(RenamedGuideResult.Success, response)
    }
}