package com.jonathanev.review.domain

import com.jonathanev.review.domain.mapper.GuideQuestionExtractor
import com.jonathanev.review.domain.model.RequiredAttrGuide
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideRenameContext
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.OptionalAttrGuide
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.ResponseDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
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
    private val navigationPathRepository = mockk<NavigationPathRepository>(relaxed = true)

    private lateinit var guideDomain: GuideDomainModel
    private var oldGuide: GuidePath = GuidePath("init")
    private var oldImage: GuidePath = GuidePath("init")
    private lateinit var context: GuideContext.Actual
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
        oldGuide = GuidePath("fake/path/oldGuide")
        oldImage = GuidePath("fake/path/oldImage")
        renameGuideUseCase = RenameGuideUseCase(
            guiaRepository = guiaRepository,
            guideQuestionExtractor = guideQuestionExtractor,
            imagesRepository = imagesRepository,
            directoryManager = directoryManager,
            navigationPathRepository = navigationPathRepository
        )
        context = GuideContext.Actual(
            guide = guideDomain
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
                question = ResponseDomain.Filled(questionItemDomain),
                answer = ResponseDomain.Filled(answerItemDomain)
            )
        )

        newName = "Nuevo archivo"
    }

    @Test
    fun unknownerror_in_get_xml_guide() {
        every {
            guiaRepository.getXMLGuide(context)
        } returns GetGuideResult.UnknownError

        renameGuideUseCase.invoke(guideDomain, newName, "")

        verify { guiaRepository.getXMLGuide(context) }
    }

    @Test
    fun error_in_get_xml_guide() {
        every {
            guiaRepository.getXMLGuide(context)
        } returns GetGuideResult.Error

        renameGuideUseCase.invoke(guideDomain, newName, "")

        verify { guiaRepository.getXMLGuide(context) }
    }

    @Test
    fun invalid_format_in_get_xml_guide() {
        every {
            guiaRepository.getXMLGuide(context)
        } returns GetGuideResult.InvalidFormat

        renameGuideUseCase.invoke(guideDomain, newName, "Prueba")

        verify { guiaRepository.getXMLGuide(context) }
    }

    @Test
    fun not_found_in_get_xml_guide() {
        every {
            guiaRepository.getXMLGuide(context)
        } returns GetGuideResult.NotFound

        renameGuideUseCase.invoke(guideDomain, newName, "")

        verify { guiaRepository.getXMLGuide(context) }
    }

    @Test
    fun guide_path_error_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.prepareGuidePath(newName)
        } returns false

        val response =
            renameGuideUseCase.invoke(guideDomain, newName, "")

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.prepareGuidePath(newName) }

        verify { navigationPathRepository.reset() }

        assertEquals(RenamedGuideResult.GuidePathError, response)
    }

    @Test
    fun renamed_error_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.prepareGuidePath(newName)
        } returns true

        every {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guideDomain,
                    RequiredAttrGuide(newName),
                    OptionalAttrGuide("")
                )
            )
        } returns false

        val response =
            renameGuideUseCase.invoke(guideDomain, newName, "")

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.prepareGuidePath(newName) }

        verify {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guideDomain,
                    RequiredAttrGuide(newName),
                    OptionalAttrGuide("")
                )
            )
        }

        verify { navigationPathRepository.reset() }

        assertEquals(RenamedGuideResult.RenamedError, response)
    }

    @Test
    fun image_error_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.prepareGuidePath(newName)
        } returns true

        every {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guideDomain,
                    RequiredAttrGuide(newName),
                    OptionalAttrGuide("")
                )
            )
        } returns true

        every {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName)
            )
        } returns false

        val response =
            renameGuideUseCase.invoke(guideDomain, newName, "")

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.prepareGuidePath(newName) }

        verify {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guideDomain,
                    RequiredAttrGuide(newName),
                    OptionalAttrGuide("")
                )
            )
        }

        verify {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName)
            )
        }

        verify { navigationPathRepository.reset() }

        assertEquals(RenamedGuideResult.ImageError, response)
    }

    @Test
    fun success_moving_guide() {
        val success = GetGuideResult.Success(guideDomainModel = guideDomain, list = result)
        every {
            guiaRepository.getXMLGuide(context)
        } returns success

        every {
            guideQuestionExtractor.map(success)
        } returns Pair(listOf(questionItemDomain), listOf(answerItemDomain))

        every {
            directoryManager.prepareGuidePath(newName)
        } returns true

        every {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guideDomain,
                    RequiredAttrGuide(newName),
                    OptionalAttrGuide("")
                )
            )
        } returns true

        every {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName)
            )
        } returns true

        val response =
            renameGuideUseCase.invoke(guideDomain, newName, "")

        verify { guideQuestionExtractor.map(success) }

        verify { directoryManager.prepareGuidePath(newName) }

        verify {
            guiaRepository.renameGuide(
                preguntas = listOf(questionItemDomain),
                respuestas = listOf(answerItemDomain),
                guideContext = GuideContext.Rename(
                    guideDomain,
                    RequiredAttrGuide(newName),
                    OptionalAttrGuide("")
                )
            )
        }

        verify {
            imagesRepository.moveImages(
                images = any(),
                guideRenameContext = GuideRenameContext(guideDomain, newName)
            )
        }

        verify { navigationPathRepository.reset() }

        assertEquals(RenamedGuideResult.Success, response)
    }
}