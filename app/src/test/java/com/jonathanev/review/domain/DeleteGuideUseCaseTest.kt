package com.jonathanev.review.domain

import com.jonathanev.review.data.repository.NavigationPathRepositoryImpl
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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Ignore

class DeleteGuideUseCaseTest {
    private val guiaRepository = mockk<GuiaRepository>()
    private val imagesRepository = mockk<ImagesRepository>()
    private val navigationPathRepository = mockk<NavigationPathRepositoryImpl>()
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
            DeleteGuideUseCase(guiaRepository, imagesRepository, navigationPathRepository)
    }

    @Ignore("Por el momento no")
    fun if_the_xml_is_not_read_correctly_it_returns_an_error() = runTest {
        coEvery {
            guiaRepository.getXMLGuide(guideDomainModel)
        } returns GetGuideResult.NotFound

        val response = deleteGuideUseCase.invoke(guideDomainModel)

        assertEquals(DeleteGuideResult.Error, response)
    }

    @Ignore("Por el momento no")
    fun error_deleting_the_guide() = runTest {
        coEvery {
            guiaRepository.getXMLGuide(guideDomainModel)
        } returns GetGuideResult.Success(guideDomainModel, listOf(qaItemDomain))

        coEvery {
            guiaRepository.deleteGuide(
                GuideContext.DeleteGuide(
                    guideDomainModel,
                    relativeGuidePath
                )
            )
        } returns false

        val response = deleteGuideUseCase.invoke(guideDomainModel)

        coVerify {
            guiaRepository.getXMLGuide(guideDomainModel)
        }

        assertEquals(DeleteGuideResult.ErrorGuide, response)
    }

    @Ignore("Por el momento no")
    fun error_deleting_the_images() = runTest {
        coEvery {
            guiaRepository.getXMLGuide(guideDomainModel)
        } returns GetGuideResult.Success(guideDomainModel, listOf(qaItemDomain))

        coEvery {
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

        val response = deleteGuideUseCase.invoke(guideDomainModel)

        coVerify {
            guiaRepository.getXMLGuide(guideDomainModel)
        }
        coVerify {
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

    @Ignore("Por el momento no")
    fun success_deliting_the_guide() = runTest {
        coEvery {
            guiaRepository.getXMLGuide(guideDomainModel)
        } returns GetGuideResult.Success(guideDomainModel, listOf(qaItemDomain))

        coEvery {
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

        val response = deleteGuideUseCase.invoke(guideDomainModel)

        coVerify {
            guiaRepository.getXMLGuide(guideDomainModel)
        }
        coVerify {
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