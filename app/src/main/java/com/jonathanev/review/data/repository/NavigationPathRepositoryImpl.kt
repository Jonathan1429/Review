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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val relativePath: StateFlow<RelativeGuidePath> = preferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            RelativeGuidePath(prefs[KEY_PATH] ?: "")
        }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Lazily,
            initialValue = RelativeGuidePath("")
        )

    override fun getRootGuides() = GuidePath(filePathsProvider.fileGuides)
    override fun getRootImages() = GuidePath(filePathsProvider.fileImages)

    override suspend fun next(fileName: String) {
        preferencesDataStore.edit { preferences ->
            val current = preferences[KEY_PATH] ?: ""
            preferences[KEY_PATH] = if (current.isBlank()) fileName else "$current/$fileName"
        }
    }

    override suspend fun back() {
        preferencesDataStore.edit { preferences ->
            val current = preferences[KEY_PATH] ?: ""
            preferences[KEY_PATH] = current.trimEnd('/').substringBeforeLast("/", "")
        }
    }

    override suspend fun reset() {
        preferencesDataStore.edit { preferences ->
            preferences[KEY_PATH] = ""
        }
    }
}