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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationPathRepositoryImpl @Inject constructor(
    private val filePathsProvider: FilePathsProvider,
    private val preferencesDataStore: DataStore<Preferences>
) : NavigationPathRepository {

    private val _lastModifiedFolder = MutableStateFlow<String?>(null)

    companion object {
        private val KEY_PATH = stringPreferencesKey("relative_path")
    }

    override fun getRelativePathFlow(): Flow<RelativeGuidePath> {
        return preferencesDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val rawPath = preferences[KEY_PATH].orEmpty()
                RelativeGuidePath(rawPath)
            }
            .distinctUntilChanged()
    }    // 2. La consulta puntual (reutiliza el Flow con .firstOrNull)

    override suspend fun getRelativePath(): RelativeGuidePath {
        return getRelativePathFlow().firstOrNull() ?: RelativeGuidePath("")
    }

    override fun getRootGuides() = GuidePath(filePathsProvider.fileGuides)
    override fun getRootImages() = GuidePath(filePathsProvider.fileImages)

    override suspend fun next(fileName: String): Result<Unit> = runCatching {
        val sanitizedPath = fileName.trim()

        withContext(Dispatchers.IO + NonCancellable) {
            preferencesDataStore.edit { preferences ->
                preferences[KEY_PATH] = sanitizedPath
            }
        }
    }

    override suspend fun reset(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO + NonCancellable) {
            preferencesDataStore.edit { preferences ->
                preferences[KEY_PATH] = ""
            }
        }
    }

    override fun getLastModifiedFolderFlow(): Flow<String?> = _lastModifiedFolder.asStateFlow()

    override suspend fun setLastModifiedFolder(folderName: String?) {
        _lastModifiedFolder.value = folderName
    }
}
