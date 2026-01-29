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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun error_creating_the_guide() {
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val image = QuestionContentDomain.Image("uri", "1.png")
        val question = QuestionItemDomain(listOf(image))
        /*val question = mockk<QuestionItemDomain> {
            every { content } returns listOf(image)
        }*/
        every { directoryManager.createPathImages(guideDomain, true) } returns false

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = true
        )

        assertFalse(response)
    }

    @Test
    fun error_relocating_existing_guide_images() {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val guidePath = GuidePath("navegacion/fake")
        val question = QuestionItemDomain(listOf(image))

        every { navigationPathRepository.getPathImages() } returns guidePath

        every {
            directoryManager.moveImages(
                guideDomain, ImageSource.SaveGuide(guidePath), listOf(image)
            )
        } returns false

        every { directoryManager.createPathImages(guideDomain, false) } returns true

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false
        )

        val resPathImages = directoryManager.createPathImages(guideDomain, false)
        assertTrue(resPathImages)

        val resMoveImages = directoryManager.moveImages(
            guideDomain,
            ImageSource.SaveGuide(guidePath),
            listOf(image)
        )
        assertFalse(resMoveImages)
        assertFalse(response)
    }

    @Test
    fun successful_process_with_new_file() {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")

        val question = QuestionItemDomain(listOf(image))
        every { directoryManager.createPathImages(guideDomain, true) } returns true
        every { directoryManager.getImagesInDevice(guideDomain) } returns setOf("2.png")

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = true
        )

        val resDirMana = directoryManager.createPathImages(guideDomain, true)
        assertTrue(resDirMana)

        val resImagesInDevice = directoryManager.getImagesInDevice(guideDomain)
        assertEquals(setOf("2.png"), resImagesInDevice)

        verify(exactly = 1) {
            imagesRepository.save(image, guideDomain)
        }

        verify {
            directoryManager.deleteLeftoverImagesInDevice(guideDomain.nameGuide, listOf(image))
        }
        assertTrue(response)
    }

    @Test
    fun successful_process_with_old_file() {
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val guidePath = GuidePath("navegacion/fake")
        val image1 = QuestionContentDomain.Image("uri", "1.png")
        val image2 = QuestionContentDomain.Image("uri", "2.png")
        val image3 = QuestionContentDomain.Image("", "3.png")
        val images = listOf(image1, image2, image3)

        val question = QuestionItemDomain(images)
        every { directoryManager.getImagesInDevice(guideDomain) } returns setOf("2.png")
        every { navigationPathRepository.getPathImages() } returns guidePath
        every { directoryManager.createPathImages(guideDomain, false) } returns true
        every {
            directoryManager.moveImages(
                guideDomain, ImageSource.SaveGuide(guidePath), images
            )
        } returns true

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false
        )

        val resDirMana = directoryManager.createPathImages(guideDomain, false)
        assertTrue(resDirMana)

        val resMoveImages = directoryManager.moveImages(
            guideDomain,
            ImageSource.SaveGuide(guidePath),
            images
        )
        assertTrue(resMoveImages)

        val resImagesInDevice = directoryManager.getImagesInDevice(guideDomain)
        assertEquals(setOf("2.png"), resImagesInDevice)

        verify(exactly = 1) {
            imagesRepository.save(image1, guideDomain)
        }

        verify(exactly = 0) {
            imagesRepository.save(image2, guideDomain)
            imagesRepository.save(image3, guideDomain)
        }

        verify {
            directoryManager.deleteLeftoverImagesInDevice(guideDomain.nameGuide, images)
        }

        assertTrue(response)
    }
}