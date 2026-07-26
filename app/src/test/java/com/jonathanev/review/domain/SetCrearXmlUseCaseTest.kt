package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import com.jonathanev.review.domain.result.GuideResource
import com.jonathanev.review.domain.result.SaveGuideErrors
import com.jonathanev.review.domain.result.UpdateGuideResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore

class SetCrearXmlUseCaseTest {
    private val setDecodePathImageUseCase = mockk<SetDecodePathImageUseCase>()
    private val loadGuidesUseCase = mockk<LoadGuidesUseCase>()
    private val setLabelsUseCase = mockk<SetLabelsUseCase>()
    private val navigationPathRepository = mockk<NavigationPathRepository>()
    private val updateImagesUseCase = mockk<UpdateImagesUseCase>()
    private val directoryManager = mockk<DirectoryManager>()
    private val guiaRepository = mockk<GuiaRepository>()
    private lateinit var setCrearXmlUseCase: SetCrearXmlUseCase

    private lateinit var nameGuide: String
    private lateinit var description: String
    private lateinit var preguntas: List<QuestionItemDomain>
    private lateinit var respuestas: List<QuestionItemDomain>
    private lateinit var guides: Flow<List<GuideDomainModel>>
    private var relativeGuidePath: RelativeGuidePath = RelativeGuidePath("init")
    private lateinit var guideDomainModel: GuideDomainModel

    @Before
    fun setUp() {
        val content = listOf(
            QuestionItemDomain(
                listOf(
                    QuestionContentDomain.Text(
                        "Texto de prueba",
                        emptyList()
                    )
                )
            )
        )

        nameGuide = "Guia de prueba"
        description = "Sin descripcion"
        preguntas = content
        respuestas = content
        relativeGuidePath = RelativeGuidePath("Abap")
        guideDomainModel = GuideDomainModel(GuideVersion.V2, "Guia de prueba", "Sin descripcion")

        guides = flowOf(
            listOf(
            GuideDomainModel(GuideVersion.V2, "Prueba", "Sin descripcion"),
            guideDomainModel
            )
        )

        setCrearXmlUseCase = SetCrearXmlUseCase(
            navigationPathRepository,
            setDecodePathImageUseCase,
            loadGuidesUseCase,
            setLabelsUseCase,
            updateImagesUseCase,
            directoryManager,
            guiaRepository
        )
    }

    @Ignore("No")
    fun error_update_guide() = runTest {
        val localGuides =
            flowOf(listOf(GuideDomainModel(GuideVersion.V2, "Prueba", "Sin descripcion")))
        coEvery { setDecodePathImageUseCase.invoke(preguntas, respuestas) } returns Pair(
            preguntas,
            respuestas
        )
        every { loadGuidesUseCase.invoke() } returns localGuides

        val response = setCrearXmlUseCase.invoke(
            nameGuide = "Matematicas",
            description = description,
            preguntas = preguntas,
            respuestas = respuestas,
            mode = SaveGuideMode.Update
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        assertEquals(UpdateGuideResult.ErrorUpdateGuide, response)
    }

    @Ignore("No")
    fun error_create_path_guide() = runTest {
        coEvery {
            setDecodePathImageUseCase.invoke(preguntas, respuestas)
        } returns Pair(
            preguntas,
            respuestas
        )
        every { loadGuidesUseCase.invoke() } returns guides
        every {
            setLabelsUseCase.invoke(preguntas, respuestas)
        } returns Pair(preguntas, respuestas)
        every { directoryManager.createPathGuide(relativeGuidePath, nameGuide) } returns false

        val response = setCrearXmlUseCase.invoke(
            nameGuide = nameGuide,
            description = description,
            preguntas = preguntas,
            respuestas = respuestas,
            mode = SaveGuideMode.Update
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        verify { directoryManager.createPathGuide(relativeGuidePath, nameGuide) }

        assertEquals(UpdateGuideResult.ErrorPath, response)
    }

    @Ignore("No")
    fun failure_create_guide() = runTest {
        coEvery {
            setDecodePathImageUseCase.invoke(preguntas, respuestas)
        } returns Pair(
            preguntas,
            respuestas
        )
        every { loadGuidesUseCase.invoke() } returns guides
        every {
            setLabelsUseCase.invoke(preguntas, respuestas)
        } returns Pair(preguntas, respuestas)
        every { directoryManager.createPathGuide(relativeGuidePath, nameGuide) } returns true
        coEvery {
            guiaRepository.saveGuide(guideDomainModel, preguntas, respuestas, relativeGuidePath)
        } returns GuideResource.Error(SaveGuideErrors.InsufficientStorageOrDiskError)

        setCrearXmlUseCase.invoke(
            nameGuide = nameGuide,
            description = description,
            preguntas = preguntas,
            respuestas = respuestas,
            mode = SaveGuideMode.Update
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        verify { directoryManager.createPathGuide(relativeGuidePath, nameGuide) }
        coVerify {
            guiaRepository.saveGuide(
                guideDomainModel,
                preguntas,
                respuestas,
                relativeGuidePath
            )
        }
    }

    @Ignore("No")
    fun failure_saved_images() = runTest {
        coEvery {
            setDecodePathImageUseCase.invoke(preguntas, respuestas)
        } returns Pair(
            preguntas,
            respuestas
        )
        every { loadGuidesUseCase.invoke() } returns guides
        every {
            setLabelsUseCase.invoke(preguntas, respuestas)
        } returns Pair(preguntas, respuestas)
        every { directoryManager.createPathGuide(relativeGuidePath, nameGuide) } returns true
        coEvery {
            guiaRepository.saveGuide(guideDomainModel, preguntas, respuestas, relativeGuidePath)
        } returns GuideResource.Success(guideDomainModel)
        every {
            updateImagesUseCase.invoke(
                GuideDomainModel(GuideVersion.V2, nameGuide, description),
                preguntas,
                respuestas,
                true,
                relativeGuidePath
            )
        } returns false

        val response = setCrearXmlUseCase.invoke(
            nameGuide = nameGuide,
            description = description,
            preguntas = preguntas,
            respuestas = respuestas,
            mode = SaveGuideMode.Create
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        verify { directoryManager.createPathGuide(relativeGuidePath, nameGuide) }
        coVerify {
            guiaRepository.saveGuide(
                guideDomainModel,
                preguntas,
                respuestas,
                relativeGuidePath
            )
        }
        verify {
            updateImagesUseCase.invoke(
                guideDomainModel,
                preguntas,
                respuestas,
                true,
                relativeGuidePath
            )
        }
        assertEquals(UpdateGuideResult.ImagesFailed, response)
    }

    @Ignore("No")
    fun successful_process() = runTest {
        coEvery {
            setDecodePathImageUseCase.invoke(preguntas, respuestas)
        } returns Pair(
            preguntas,
            respuestas
        )
        every { loadGuidesUseCase.invoke() } returns guides
        every {
            setLabelsUseCase.invoke(preguntas, respuestas)
        } returns Pair(preguntas, respuestas)
        every { directoryManager.createPathGuide(relativeGuidePath, nameGuide) } returns true
        coEvery {
            guiaRepository.saveGuide(guideDomainModel, preguntas, respuestas, relativeGuidePath)
        } returns GuideResource.Success(guideDomainModel)
        every {
            updateImagesUseCase.invoke(
                GuideDomainModel(GuideVersion.V2, nameGuide, description),
                preguntas,
                respuestas,
                false,
                relativeGuidePath
            )
        } returns true

        val response = setCrearXmlUseCase.invoke(
            nameGuide = nameGuide,
            description = description,
            preguntas = preguntas,
            respuestas = respuestas,
            mode = SaveGuideMode.Update
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        verify { directoryManager.createPathGuide(relativeGuidePath, nameGuide) }
        coVerify {
            guiaRepository.saveGuide(
                guideDomainModel,
                preguntas,
                respuestas,
                relativeGuidePath
            )
        }
        verify {
            updateImagesUseCase.invoke(
                guideDomain = guideDomainModel,
                preguntasProcesadas = preguntas,
                respuestasProcesadas = respuestas,
                isNewFile = false,
                relativeGuidePath = relativeGuidePath
            )
        }
        assertEquals(UpdateGuideResult.Success, response)
    }
}