package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderDomainModel


interface FolderRepository {
    fun getFolders(): List<FolderDomainModel>
    suspend fun deleteFolder(): Boolean
}