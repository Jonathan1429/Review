package com.jonathanev.review.domain.repository

import com.jonathanev.review.data.model.json.ScreenDataDto

interface MetadataRepository {
    fun saveMetadata(data: ScreenDataDto)
}