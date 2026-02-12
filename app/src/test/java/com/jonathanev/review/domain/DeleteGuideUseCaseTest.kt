package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QAItemDomain
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.DeleteGuideResult
import com.jonathanev.review.domain.result.GetGuideResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteGuideUseCaseTest {
    private val guiaRepository = mockk<GuiaRepository>()
    private val imagesRepository = mockk<ImagesRepository>()
    private lateinit var deleteGuideUseCase: DeleteGuideUseCase
    private lateinit var guideDomainModel: GuideDomainModel
    private var relativeGuidePath = RelativeGuidePath("init")
    private lateinit var qaItemDomain: QAItemDomain

    @Before
    fun setUp() {
        guideDomainModel = GuideDomainModel(GuideVersion.V2, "Guia a eliminar", "")
        relativeGuidePath = RelativeGuidePath("Kotlin")
        qaItemDomain = QAItemDomain(
            question = QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text("Pregunta", emptyList())
                )
            ),
            answer = QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text("Respuesta", emptyList())
                )
            )
        )

        deleteGuideUseCase =
            DeleteGuideUseCase(guiaRepository, imagesRepository)
    }

    @Test
    fun if_the_xml_is_not_read_correctly_it_returns_an_error() {
        every {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        } returns GetGuideResult.NotFound

        val response = deleteGuideUseCase.invoke(guideDomainModel, relativeGuidePath)

        assertEquals(DeleteGuideResult.Error, response)
    }

    @Test
    fun error_deleting_the_guide() {
        every {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        } returns GetGuideResult.Success(guideDomainModel, listOf(qaItemDomain))

        every {
            guiaRepository.deleteGuide(
                GuideContext.DeleteGuide(
                    guideDomainModel,
                    relativeGuidePath
                )
            )
        } returns false

        val response = deleteGuideUseCase.invoke(guideDomainModel, relativeGuidePath)

        verify {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        }

        assertEquals(DeleteGuideResult.ErrorGuide, response)
    }

    @Test
    fun error_deleting_the_images() {
        every {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        } returns GetGuideResult.Success(guideDomainModel, listOf(qaItemDomain))

        every {
            guiaRepository.deleteGuide(
                GuideContext.DeleteGuide(
                    guideDomainModel,
                    relativeGuidePath
                )
            )
        } returns true

        every {
            imagesRepository.deleteImages(
                guideDomainModel,
                emptyList(),
                relativeGuidePath
            )
        } returns false

        val response = deleteGuideUseCase.invoke(guideDomainModel, relativeGuidePath)

        verify {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        }
        verify {
            guiaRepository.deleteGuide(
                GuideContext.DeleteGuide(
                    guideDomainModel,
                    relativeGuidePath
                )
            )
        }

        verify { imagesRepository.deleteImages(guideDomainModel, emptyList(), relativeGuidePath) }
        assertEquals(DeleteGuideResult.ErrorImage, response)
    }

    @Test
    fun success_deliting_the_guide() {
        every {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        } returns GetGuideResult.Success(guideDomainModel, listOf(qaItemDomain))

        every {
            guiaRepository.deleteGuide(
                GuideContext.DeleteGuide(
                    guideDomainModel,
                    relativeGuidePath
                )
            )
        } returns true

        every {
            imagesRepository.deleteImages(
                guideDomainModel,
                emptyList(),
                relativeGuidePath
            )
        } returns true

        val response = deleteGuideUseCase.invoke(guideDomainModel, relativeGuidePath)

        verify {
            guiaRepository.getXMLGuide(
                guideDomainModel,
                relativeGuidePath
            )
        }
        verify {
            guiaRepository.deleteGuide(
                GuideContext.DeleteGuide(
                    guideDomainModel,
                    relativeGuidePath
                )
            )
        }

        verify { imagesRepository.deleteImages(guideDomainModel, emptyList(), relativeGuidePath) }
        assertEquals(DeleteGuideResult.DeleteSuccess, response)
    }
}