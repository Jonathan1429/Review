package com.jonathanev.review.domain.repository

import com.jonathanev.review.domain.model.FolderScreenInfoDomain

interface MetadataRepository {
    fun saveMetadata(data: FolderScreenInfoDomain)
}