package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.GuideVersion
import com.jonathanev.review.domain.model.QuestionContentDomain
import com.jonathanev.review.domain.model.QuestionItemDomain
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.model.SaveGuideMode
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaRepository
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
import org.junit.Test

class SetCrearXmlUseCaseTest {
    private val setDecodePathImageUseCase = mockk<SetDecodePathImageUseCase>()
    private val loadGuidesUseCase = mockk<LoadGuidesUseCase>()
    private val setLabelsUseCase = mockk<SetLabelsUseCase>()
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
            setDecodePathImageUseCase,
            loadGuidesUseCase,
            setLabelsUseCase,
            updateImagesUseCase,
            directoryManager,
            guiaRepository
        )
    }

    @Test
    fun error_update_guide() = runTest {
        val localGuides =
            flowOf(listOf(GuideDomainModel(GuideVersion.V2, "Prueba", "Sin descripcion")))
        coEvery { setDecodePathImageUseCase.invoke(preguntas, respuestas) } returns Pair(
            preguntas,
            respuestas
        )
        every { loadGuidesUseCase.invoke() } returns localGuides

        val response = setCrearXmlUseCase.invoke(
            guideDomainModel = guideDomainModel,
            preguntas = preguntas,
            respuestas = respuestas,
            saveGuideMode = SaveGuideMode.Update
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        assertEquals(UpdateGuideResult.ErrorUpdateGuide, response)
    }

    @Test
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
        coEvery { directoryManager.createPathGuide(guideDomainModel) } returns false

        val response = setCrearXmlUseCase.invoke(
            guideDomainModel = guideDomainModel,
            preguntas = preguntas,
            respuestas = respuestas,
            saveGuideMode = SaveGuideMode.Update
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        coVerify { directoryManager.createPathGuide(guideDomainModel) }

        assertEquals(UpdateGuideResult.ErrorPath, response)
    }

    @Test
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
        coEvery { directoryManager.createPathGuide(guideDomainModel) } returns true
        coEvery {
            guiaRepository.saveGuide(guideDomainModel, preguntas, respuestas)
        } returns GuideResource.Error(SaveGuideErrors.InsufficientStorageOrDiskError)

        setCrearXmlUseCase.invoke(
            guideDomainModel = guideDomainModel,
            preguntas = preguntas,
            respuestas = respuestas,
            saveGuideMode = SaveGuideMode.Update
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        coVerify { directoryManager.createPathGuide(guideDomainModel) }
        coVerify {
            guiaRepository.saveGuide(
                guideDomainModel,
                preguntas,
                respuestas
            )
        }
    }

    @Test
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
        coEvery { directoryManager.createPathGuide(guideDomainModel) } returns true
        coEvery {
            guiaRepository.saveGuide(guideDomainModel, preguntas, respuestas)
        } returns GuideResource.Success(guideDomainModel)
        coEvery {
            updateImagesUseCase.invoke(
                guideDomain = guideDomainModel,
                preguntasProcesadas = preguntas,
                respuestasProcesadas = respuestas,
                saveGuideMode = SaveGuideMode.Create
            )
        } returns false

        val response = setCrearXmlUseCase.invoke(
            guideDomainModel = guideDomainModel,
            preguntas = preguntas,
            respuestas = respuestas,
            saveGuideMode = SaveGuideMode.Create
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        //verify { loadGuidesUseCase.invoke() }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        coVerify { directoryManager.createPathGuide(guideDomainModel) }
        coVerify {
            guiaRepository.saveGuide(
                guideDomainModel,
                preguntas,
                respuestas
            )
        }
        coVerify {
            updateImagesUseCase.invoke(
                guideDomain = guideDomainModel,
                preguntasProcesadas = preguntas,
                respuestasProcesadas = respuestas,
                saveGuideMode = SaveGuideMode.Create
            )
        }
        assertEquals(UpdateGuideResult.ImagesFailed, response)
    }

    @Test
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
        coEvery { directoryManager.createPathGuide(guideDomainModel) } returns true
        coEvery {
            guiaRepository.saveGuide(guideDomainModel, preguntas, respuestas)
        } returns GuideResource.Success(guideDomainModel)
        coEvery {
            updateImagesUseCase.invoke(
                guideDomain = guideDomainModel,
                preguntasProcesadas = preguntas,
                respuestasProcesadas = respuestas,
                saveGuideMode = SaveGuideMode.Create
            )
        } returns true

        val response = setCrearXmlUseCase.invoke(
            guideDomainModel = guideDomainModel,
            preguntas = preguntas,
            respuestas = respuestas,
            saveGuideMode = SaveGuideMode.Create
        )

        coVerify { setDecodePathImageUseCase.invoke(preguntas, respuestas) }
        verify { setLabelsUseCase.invoke(preguntas, respuestas) }
        coVerify { directoryManager.createPathGuide(guideDomainModel) }
        coVerify {
            guiaRepository.saveGuide(
                guideDomainModel,
                preguntas,
                respuestas
            )
        }
        coVerify {
            updateImagesUseCase.invoke(
                guideDomain = guideDomainModel,
                preguntasProcesadas = preguntas,
                respuestasProcesadas = respuestas,
                saveGuideMode = SaveGuideMode.Create,
            )
        }
        assertEquals(UpdateGuideResult.Success, response)
    }
}