package com.jonathanev.review.data.repository

import com.jonathanev.review.data.datastore.preferences.UserPreferencesDataStore
import com.jonathanev.review.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
): UserPreferencesRepository {
    override fun getCountImage(): Flow<Int> = dataStore.getCountImage()

    override suspend fun setImageCount(count: Int) = dataStore.setCounter(count)

    override suspend fun setDontAskDelete(value: Boolean) = dataStore.setDontAskDelete(value)

    override fun getDontAskDelete(): Flow<Boolean> = dataStore.getDontAskDelete()
}