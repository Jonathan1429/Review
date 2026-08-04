package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderScreenInfoDomain

interface MetadataRepository {
    suspend fun saveMetadata(data: FolderScreenInfoDomain)
}