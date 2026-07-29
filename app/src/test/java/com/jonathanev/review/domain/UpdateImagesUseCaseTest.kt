package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.ImageContext
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.ImagesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
    fun error_creating_the_guide() = runTest {
        val guideDomain = GuideDomainModel(GuideVersion.V2, "Prueba", "Descripcion")
        val image = QuestionContentDomain.Image("uri", "1.png")
        val question = QuestionItemDomain(listOf(image))

        coEvery {
            directoryManager.createPathImages(
                guideDomain,
                true
            )
        } returns false

        val response = useCase.invoke(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            saveGuideMode = SaveGuideMode.Create
        )

        coVerify { directoryManager.createPathImages(guideDomain, true) }
        assertFalse(response)
    }

    @Test
    fun error_relocating_existing_guide_images() = runTest {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val question = QuestionItemDomain(listOf(image))


        coEvery {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                isNewFile = false
            )
        } returns true
        coEvery {
            directoryManager.moveImages(
                guideDomain, ImageContext.Save, listOf(image)
            )
        } returns false

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            saveGuideMode = SaveGuideMode.Update
        )

        coVerify {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                isNewFile = false
            )
        }
        coVerify {
            directoryManager.moveImages(
                guideDomain,
                ImageContext.Save,
                listOf(image)
            )
        }
        assertFalse(response)
    }

    @Test
    fun successful_process_with_new_file() = runTest {
        val image = QuestionContentDomain.Image("uri", "1.png")
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")

        val question = QuestionItemDomain(listOf(image))
        coEvery {
            directoryManager.createPathImages(
                GuideDomainModel(GuideVersion.V2, "Prueba", "Descripcion"),
                true
            )
        } returns true
        coEvery {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                )
            )
        } returns setOf("2.png")

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            saveGuideMode = SaveGuideMode.Create,
        )

        coVerify {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                isNewFile = true
            )
        }
        coVerify {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                )
            )
        }
        coVerify { imagesRepository.save(image, guideDomain) }
        coVerify {
            directoryManager.deleteLeftoverImagesInDevice(
                guideDomainModel = guideDomain,
                listImages = listOf(image)
            )
        }
        assertTrue(response)
    }

    @Test
    fun successful_process_with_old_file() = runTest {
        val guideDomain = GuideDomainModel(GuideVersion.V1, "Prueba", "Descripcion")
        val image1 = QuestionContentDomain.Image("uri", "1.png")
        val image2 = QuestionContentDomain.Image("uri", "2.png")
        val image3 = QuestionContentDomain.Image("", "3.png")
        val images = listOf(image1, image2, image3)

        val question = QuestionItemDomain(images)
        coEvery {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(GuideVersion.V2, "Prueba", "Descripcion"),
                isNewFile = false
            )
        } returns true
        coEvery {
            directoryManager.moveImages(
                guideDomain, ImageContext.Save, images
            )
        } returns true
        coEvery {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                )
            )
        } returns setOf("2.png")

        val response = useCase(
            guideDomain = guideDomain,
            preguntasProcesadas = listOf(question),
            respuestasProcesadas = emptyList(),
            saveGuideMode = SaveGuideMode.Update,
        )

        coVerify {
            directoryManager.createPathImages(
                guideDomainModel = GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                ),
                isNewFile = false
            )
        }
        coVerify {
            directoryManager.getImagesInDevice(
                GuideDomainModel(
                    version = GuideVersion.V2,
                    nameGuide = "Prueba",
                    description = "Descripcion"
                )
            )
        }
        coVerify(exactly = 1) {
            imagesRepository.save(image1, guideDomain)
        }
        coVerify(exactly = 0) {
            imagesRepository.save(image2, guideDomain)
            imagesRepository.save(image3, guideDomain)
        }
        coVerify {
            directoryManager.deleteLeftoverImagesInDevice(
                guideDomainModel = guideDomain,
                listImages = images
            )
        }

        assertTrue(response)
    }
}