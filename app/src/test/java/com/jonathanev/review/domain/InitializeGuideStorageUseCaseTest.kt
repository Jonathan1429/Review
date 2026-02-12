package com.jonathanev.review.domain

import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.GuiaMigrationRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.result.MigrationResult
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InitializeGuideStorageUseCaseTest {
    @MockK
    lateinit var directoryManager: DirectoryManager

    @MockK
    lateinit var guiaMigrationRepository: GuiaMigrationRepository
    private val imagesRepository = mockk<ImagesRepository>(relaxed = true)
    private lateinit var initializeGuideStorageUseCase: InitializeGuideStorageUseCase
    private lateinit var list: List<String>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        list = listOf("Abap.xml", "Kotlin.xml")

        initializeGuideStorageUseCase = InitializeGuideStorageUseCase(
            directoryManager,
            guiaMigrationRepository,
            imagesRepository
        )
    }

    @Test
    fun error_creating_main_routes() {
        every { directoryManager.createFoldersMain() } returns false

        val response = initializeGuideStorageUseCase.invoke()
        assertFalse(response)
    }

    @Test
    fun error_moving_guides_in_main_route() {
        val migrationResult = MigrationResult(emptyList(), list)
        every { directoryManager.createFoldersMain() } returns true

        every { guiaMigrationRepository.moveGuides() } returns migrationResult

        val response = initializeGuideStorageUseCase.invoke()

        verify { directoryManager.createFoldersMain() }

        verify { guiaMigrationRepository.moveGuides() }

        assertFalse(response)
    }

    @Test
    fun success_moving_guides_in_main_route() {
        val migrationResult = MigrationResult(list, emptyList())
        every { directoryManager.createFoldersMain() } returns true

        every { guiaMigrationRepository.moveGuides() } returns migrationResult

        every { imagesRepository.moveUnassignedImages(list) } just Runs

        val response = initializeGuideStorageUseCase.invoke()

        verify { directoryManager.createFoldersMain() }

        verify { guiaMigrationRepository.moveGuides() }

        verify { imagesRepository.moveUnassignedImages(list) }

        assertTrue(response)
    }
}