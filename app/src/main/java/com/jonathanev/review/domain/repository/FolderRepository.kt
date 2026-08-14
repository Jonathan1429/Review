package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderDomainModel
import com.jonathanev.review.domain.model.FolderScreenInfoDomain
import kotlinx.coroutines.flow.Flow


interface FolderRepository {
    fun getFolders(): Flow<List<FolderDomainModel>>
    suspend fun deleteFolder(): Boolean
    suspend fun createFolder(data: FolderScreenInfoDomain): Boolean
    suspend fun renameFolder(
        oldName: String,
        newName: String,
        data: FolderScreenInfoDomain
    ): Boolean
}
