package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class UpdateImagesUseCaseTest {
    private val directoryManager = mockk<DirectoryManager>(relaxed = true)
    private val imagesRepository = mockk<ImagesRepository>(relaxed = true)
    private val navigationPathRepository = mockk<NavigationPathRepository>()

    private lateinit var useCase: UpdateImagesUseCase
    private val guidePath = GuidePath("fake/path")

    @Before
    fun setup() {
        every { navigationPathRepository.getPathImages() } returns guidePath

        useCase = UpdateImagesUseCase(
            directoryManager,
            imagesRepository,
            navigationPathRepository
        )
    }

    @Test
    fun `when new file and images are saved`() {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")

        val question = mockk<QuestionItemDomain> {
            every { content } returns listOf(image)
        }
        every { directoryManager.getImagesInDevice(guideDomain) } returns setOf("2.png")

        useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = true
        )

        verify(exactly = 1) { directoryManager.createPathImages(guideDomain, true) }

        verify(exactly = 1) { directoryManager.getImagesInDevice(guideDomain) }

        verify(exactly = 1) {
            imagesRepository.save(image, guideDomain)
        }

        verify(exactly = 1) {
            directoryManager.deleteLeftoverImagesInDevice(guideDomain.nameGuide, listOf(image))
        }
    }

    @Test
    fun `when the file exists and everything goes correctly`() {
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val guidePath = GuidePath("navegacion/fake")
        val images = listOf(
            QuestionContentDomain.Image("uri", "1.png"),
            QuestionContentDomain.Image("uri", "2.png"),
            QuestionContentDomain.Image("", "3.png")
        )

        val question = mockk<QuestionItemDomain> {
            every { content } returns images
        }
        every { directoryManager.getImagesInDevice(guideDomain) } returns setOf("2.png")
        every { navigationPathRepository.getPathImages() } returns guidePath

        every {
            directoryManager.moveImages(
                guideDomain, ImageSource.SaveGuide(guidePath), images
            )
        } returns true

        useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false
        )

        verify(exactly = 1) { directoryManager.createPathImages(guideDomain, false) }

        verify(exactly = 1) {
            directoryManager.moveImages(
                guideDomain,
                ImageSource.SaveGuide(guidePath),
                images
            )
        }

        verify(exactly = 1) { directoryManager.getImagesInDevice(guideDomain) }

        verify(exactly = 1) {
            imagesRepository.save(any(), guideDomain)
        }


        verify(exactly = 1) {
            directoryManager.deleteLeftoverImagesInDevice(guideDomain.nameGuide, images)
        }
    }

    @Test
    fun `when the file exists and moving images fails`() {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val guidePath = GuidePath("navegacion/fake")

        val question = mockk<QuestionItemDomain> {
            every { content } returns listOf(image)
        }
        every { directoryManager.getImagesInDevice(guideDomain) } returns setOf("2.png")
        every { navigationPathRepository.getPathImages() } returns guidePath

        every {
            directoryManager.moveImages(
                guideDomain, ImageSource.SaveGuide(guidePath), listOf(image)
            )
        } returns false

        useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false
        )

        verify(exactly = 1) { directoryManager.createPathImages(guideDomain, false) }

        verify(exactly = 1) {
            directoryManager.moveImages(
                guideDomain,
                ImageSource.SaveGuide(guidePath),
                listOf(image)
            )
        }
    }

    /*@Test
    fun `Process when the file has no images`() {
        val text = QuestionContentDomain.Text("Texto de prueba", emptyList())
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val guidePath = GuidePath("navegacion/fake")

        val question = mockk<QuestionItemDomain> {
            every { content } returns listOf(text)
        }
        every { directoryManager.getImagesInDevice(guideDomain) } returns setOf("2.png")
        every { navigationPathRepository.getPathImages() } returns guidePath

        every {
            directoryManager.moveImages(
                guideDomain, ImageSource.SaveGuide(guidePath), emptyList()
            )
        } returns true

        useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false
        )

        verify(exactly = 1) { directoryManager.createPathImages(guideDomain, false) }

        verify(exactly = 1) {
            directoryManager.moveImages(
                guideDomain,
                ImageSource.SaveGuide(guidePath),
                emptyList()
            )
        }

        verify(exactly = 1) { directoryManager.getImagesInDevice(guideDomain) }

        verify(exactly = 1) {
            directoryManager.deleteLeftoverImagesInDevice(guideDomain.nameGuide, emptyList())
        }
    }*/
}