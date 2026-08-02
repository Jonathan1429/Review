package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderDomainModel
import kotlinx.coroutines.flow.Flow


interface FolderRepository {
    fun getFolders(): Flow<List<FolderDomainModel>>
    suspend fun deleteFolder(): Boolean
}