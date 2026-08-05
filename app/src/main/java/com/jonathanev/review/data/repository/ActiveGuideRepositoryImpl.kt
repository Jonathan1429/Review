package com.jonathanev.review.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathanev.review.data.mapper.xml.toGuideVersion
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.repository.ActiveGuideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveGuideRepositoryImpl @Inject constructor(
    private val preferencesDataStore: DataStore<Preferences>
) : ActiveGuideRepository {
    companion object {
        private val KEY_VERSION = stringPreferencesKey("active_guide_version")
        private val KEY_NAME = stringPreferencesKey("active_guide_name")
        private val KEY_DESCRIPTION = stringPreferencesKey("active_guide_description")
    }

    override val activeGuideFlow: Flow<GuideDomainModel?> = preferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            val stringVersion = prefs[KEY_VERSION] ?: "-1"
            val version = stringVersion.toGuideVersion()
            val name = prefs[KEY_NAME] ?: return@map null
            val description = prefs[KEY_DESCRIPTION].orEmpty()

            GuideDomainModel(
                version = version,
                nameGuide = name,
                description = description
            )
        }

    override suspend fun setActiveGuide(guide: GuideDomainModel) {
        preferencesDataStore.edit { preferences ->
            preferences[KEY_VERSION] = guide.version.name
            preferences[KEY_NAME] = guide.nameGuide
            preferences[KEY_DESCRIPTION] = guide.description
        }
    }

    override suspend fun clearActiveGuide() {
        preferencesDataStore.edit { preferences ->
            preferences.remove(KEY_VERSION)
            preferences.remove(KEY_NAME)
            preferences.remove(KEY_DESCRIPTION)
        }
    }
}