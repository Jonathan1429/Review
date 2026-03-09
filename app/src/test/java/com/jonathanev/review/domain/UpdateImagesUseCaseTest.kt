package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageSource
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.ImagesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateImagesUseCaseTest {
    private val directoryManager = mockk<DirectoryManager>(relaxed = true)
    private val imagesRepository = mockk<ImagesRepository>(relaxed = true)
    private lateinit var useCase: UpdateImagesUseCase
    private val relativeGuidePath = RelativeGuidePath("fake/path")

    @Before
    fun setup() {
        useCase = UpdateImagesUseCase(
            directoryManager,
            imagesRepository,
        )
    }

    @Test
    fun error_creating_the_guide() {
        val guideDomain = GuideDomainModel(GuideVersion.V2, "Prueba", "Descripcion")
        val image = QuestionContentDomain.Image("uri", "1.png")
        val question = QuestionItemDomain(listOf(image))

        every {
            directoryManager.createPathImages(
                guideDomain,
                true,
                relativeGuidePath
            )
        } returns false

        val response = useCase.invoke(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = true,
            relativeGuidePath = relativeGuidePath
        )

        verify { directoryManager.createPathImages(guideDomain, true, relativeGuidePath) }
        assertFalse(response)
    }

    @Test
    fun error_relocating_existing_guide_images() {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val question = QuestionItemDomain(listOf(image))


        every {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                isNewFile = false,
                relativePath = relativeGuidePath
            )
        } returns true
        every {
            directoryManager.moveImages(
                guideDomain, ImageSource.Save(relativeGuidePath), listOf(image)
            )
        } returns false

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false,
            relativeGuidePath = relativeGuidePath
        )

        verify {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                isNewFile = false,
                relativePath = relativeGuidePath
            )
        }
        verify {
            directoryManager.moveImages(
                guideDomain,
                ImageSource.Save(relativeGuidePath),
                listOf(image)
            )
        }
        assertFalse(response)
    }

    @Test
    fun successful_process_with_new_file() {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")

        val question = QuestionItemDomain(listOf(image))
        every {
            directoryManager.createPathImages(
                GuideDomainModel(GuideVersion.V2, "Prueba", "Descripcion"),
                true,
                relativeGuidePath
            )
        } returns true
        every {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ), relativeGuidePath
            )
        } returns setOf("2.png")

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = true,
            relativeGuidePath = relativeGuidePath
        )

        verify {
            directoryManager.createPathImages(
                GuideDomainModel(
                    GuideVersion.V2,
                    "Prueba",
                    "Descripcion"
                ), true, relativeGuidePath
            )
        }
        verify {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ), relativeGuidePath
            )
        }
        verify { imagesRepository.save(image, guideDomain, relativeGuidePath) }
        verify {
            directoryManager.deleteLeftoverImagesInDevice(
                guideDomain.nameGuide,
                listOf(image),
                relativeGuidePath
            )
        }
        assertTrue(response)
    }

    @Test
    fun successful_process_with_old_file() {
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val image1 = QuestionContentDomain.Image("uri", "1.png")
        val image2 = QuestionContentDomain.Image("uri", "2.png")
        val image3 = QuestionContentDomain.Image("", "3.png")
        val images = listOf(image1, image2, image3)

        val question = QuestionItemDomain(images)
        every {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(GuideVersion.V2, "Prueba", "Descripcion"),
                isNewFile = false,
                relativePath = relativeGuidePath
            )
        } returns true
        every {
            directoryManager.moveImages(
                guideDomain, ImageSource.Save(relativeGuidePath), images
            )
        } returns true
        every {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                relativeGuidePath
            )
        } returns setOf("2.png")

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            isNewFile = false,
            relativeGuidePath = relativeGuidePath
        )

        verify {
            directoryManager.createPathImages(
                GuideDomainModel(
                    GuideVersion.V2,
                    "Prueba",
                    "Descripcion"
                ), false, relativeGuidePath
            )
        }
        verify {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                relativeGuidePath
            )
        }
        verify(exactly = 1) {
            imagesRepository.save(image1, guideDomain, relativeGuidePath)
        }
        verify(exactly = 0) {
            imagesRepository.save(image2, guideDomain, relativeGuidePath)
            imagesRepository.save(image3, guideDomain, relativeGuidePath)
        }
        verify {
            directoryManager.deleteLeftoverImagesInDevice(
                guideDomain.nameGuide,
                images,
                relativeGuidePath
            )
        }

        assertTrue(response)
    }
}