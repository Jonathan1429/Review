package com.jonathanev.review.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getCountImage(): Flow<Int>
    suspend fun setImageCount(count: Int)
    suspend fun setDontAskDelete(value: Boolean)
    fun getDontAskDelete(): Flow<Boolean>

}