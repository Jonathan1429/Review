package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.repository.FolderRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IsExistFolderUseCaseTest {
    private val folderRepository = mockk<FolderRepository>()
    private lateinit var isExisteFolderUseCase: IsExistFolderUseCase
    private lateinit var foldersDomain: List<FolderDomainModel>

    @Before
    fun setUp() {
        foldersDomain = listOf(
            FolderDomainModel(FolderAttributesDomain("Abap", "", 0), 0),
            FolderDomainModel(FolderAttributesDomain("Kotlin", "", 0), 0)
        )

        isExisteFolderUseCase = IsExistFolderUseCase(folderRepository)
    }

    @Test
    fun the_folder_exists(){
        every { folderRepository.getFolders() } returns foldersDomain

        val response = isExisteFolderUseCase.invoke("Abap")

        verify { folderRepository.getFolders() }

        assertTrue(response)
    }

    @Test
    fun the_folder_does_not_exist(){
        every { folderRepository.getFolders() } returns foldersDomain

        val response = isExisteFolderUseCase.invoke("Sql")

        verify { folderRepository.getFolders() }

        assertFalse(response)
    }
}