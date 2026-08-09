package com.jonathanev.review.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathanev.review.domain.model.GuidePath
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPathRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val preferencesDataStore: DataStore<Preferences>
) : NavigationPathRepository {

    companion object {
        private val KEY_PATH = stringPreferencesKey("relative_path")
    }

    override suspend fun getRelativePath(): RelativeGuidePath {
        return runCatching {
            preferencesDataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { prefs ->
                    val rawPath = prefs[KEY_PATH].orEmpty()
                    RelativeGuidePath(rawPath)
                }
                .firstOrNull()
        }.getOrNull() ?: RelativeGuidePath("")
    }

    override fun getRootGuides() = GuidePath(filePathsProvider.fileGuides)
    override fun getRootImages() = GuidePath(filePathsProvider.fileImages)

    override suspend fun next(fileName: String): Result<Unit> = runCatching {
        val sanitizedPath = fileName.trim()
        preferencesDataStore.edit { preferences ->
            preferences[KEY_PATH] = sanitizedPath
        }
        Unit
    }

    override suspend fun reset(): Result<Unit> = runCatching {
        preferencesDataStore.edit { preferences ->
            preferences[KEY_PATH] = ""
        }
        Unit
    }
}