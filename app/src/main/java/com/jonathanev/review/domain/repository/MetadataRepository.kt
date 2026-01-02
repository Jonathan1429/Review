package com.jonathanev.review.domain.repository

import com.jonathanev.review.presentation.model.ScreenData

interface MetadataRepository {
    fun saveMetadata(data: ScreenData)
}