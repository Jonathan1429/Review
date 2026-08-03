package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsExistFolderUseCaseTest {
    private val folderRepository = mockk<FolderRepository>()
    private lateinit var isExistFolderUseCase: IsExistFolderUseCase
    private lateinit var foldersDomain: List<FolderDomainModel>

    @Before
    fun setUp() {
        foldersDomain = listOf(
            FolderDomainModel(FolderAttributesDomain("Abap", "", 0), 0),
            FolderDomainModel(FolderAttributesDomain("Kotlin", "", 0), 0)
        )

        isExistFolderUseCase = IsExistFolderUseCase(folderRepository)
    }

    @Test
    fun the_folder_exists() = runTest {
        // 1. ARRANGE (Preparación)
        every { folderRepository.getFolders() } returns flowOf(foldersDomain)

        // 2. ACT (Ejecución)
        val result = isExistFolderUseCase.invoke("Abap")

        verify(exactly = 1) {
            val folders = folderRepository.getFolders()
        }
        assertTrue(result)
    }

    @Test
    fun the_folder_does_not_exist() = runTest {
        every { folderRepository.getFolders() } returns flowOf(foldersDomain)

        val response = isExistFolderUseCase.invoke("Sql")

        verify(exactly = 1) {
            val folders = folderRepository.getFolders()
        }
        assertFalse(response)
    }
}