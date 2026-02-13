package com.jonathanev.review.domain

import com.jonathanev.review.domain.model.FolderAttributesDomain
import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.result.FolderResultDomain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetFolderPosicionUseCaseTest {
    private lateinit var getFolderPosicionUseCase: GetFolderPosicionUseCase
    private lateinit var foldersDomain: List<FolderDomainModel>
    private lateinit var folder: FolderDomainModel

    @Before
    fun setUp() {
        folder = FolderDomainModel(FolderAttributesDomain("Kotlin", "", 0), 0)
        foldersDomain = listOf(
            FolderDomainModel(FolderAttributesDomain("Abap", "", 0), 0),
            folder
        )

        getFolderPosicionUseCase = GetFolderPosicionUseCase()
    }

    @Test
    fun error_searching_for_folder() {
        val response = getFolderPosicionUseCase.invoke(2, foldersDomain)

        assertEquals(
            FolderResultDomain.Error("No se encontró la carpeta en la posición 2"),
            response
        )
    }

    @Test
    fun success_searching_for_folder() {
        val response = getFolderPosicionUseCase.invoke(1, foldersDomain)

        assertEquals(
            FolderResultDomain.Success(folder), response
        )
    }
}