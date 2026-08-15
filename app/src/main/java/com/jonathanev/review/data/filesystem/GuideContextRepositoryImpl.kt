package com.jonathanev.review.data.filesystem

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jonathanev.review.data.mapper.xml.toGuideVersion
import com.jonathanev.review.domain.model.GuideContext
import com.jonathanev.review.domain.model.GuideDomainModel
import com.jonathanev.review.domain.model.RelativeGuidePath
import com.jonathanev.review.domain.repository.GuideContextRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuideContextRepositoryImpl @Inject constructor(
    private val preferencesDataStore: DataStore<Preferences>,
    private val navigationPathRepository: NavigationPathRepository
) : GuideContextRepository {

    companion object {
        private val KEY_CONTEXT_TYPE = stringPreferencesKey("guide_context_type")
        private val KEY_VERSION = stringPreferencesKey("guide_context_version")
        private val KEY_NAME = stringPreferencesKey("guide_context_name")
        private val KEY_DESCRIPTION = stringPreferencesKey("guide_context_description")
        private val KEY_OLD_RELATIVE_PATH = stringPreferencesKey("guide_context_old_relative_path")
        private val KEY_POSITION = intPreferencesKey("guide_context_position")

        private const val TYPE_CREATING = "CREATING"
        private const val TYPE_MOVING = "MOVING"
        private const val TYPE_BROWSING = "BROWSING"
        private const val TYPE_EDITING = "EDITING"
        private const val TYPE_DELETE = "DELETE"
    }

    override val guideContext: Flow<GuideContext?> = preferencesDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            val type = prefs[KEY_CONTEXT_TYPE] ?: return@map null
            val stringVersion = prefs[KEY_VERSION] ?: "-1"
            val version = stringVersion.toGuideVersion()
            val name = prefs[KEY_NAME] ?: return@map null
            val description = prefs[KEY_DESCRIPTION].orEmpty()
            val position = prefs[KEY_POSITION] ?: 0

            val guide = GuideDomainModel(
                version = version,
                nameGuide = name,
                description = description
            )

            // Reconstruimos el objeto desde los primitivos guardados en disco
            when (type) {
                TYPE_CREATING -> GuideContext.Creating(guide)
                TYPE_MOVING -> {
                    val oldPathStr = prefs[KEY_OLD_RELATIVE_PATH].orEmpty()
                    GuideContext.Moving(
                        guide = guide,
                        oldRelativeGuidePath = RelativeGuidePath(oldPathStr)
                    )
                }

                TYPE_BROWSING -> GuideContext.Browsing(guide = guide, position = position)
                TYPE_EDITING -> GuideContext.Editing(guide = guide, position = position)
                TYPE_DELETE -> GuideContext.DeleteGuide(guide)
                else -> null
            }
        }

    override suspend fun start(guideContext: GuideContext): Result<Unit> = runCatching {
        val finalContext = if (guideContext is GuideContext.Moving) {
            val relativePath = navigationPathRepository.getRelativePath()
            guideContext.copy(oldRelativeGuidePath = relativePath)
        } else {
            guideContext
        }

        preferencesDataStore.edit { preferences ->
            when (finalContext) {
                is GuideContext.Creating -> {
                    preferences[KEY_CONTEXT_TYPE] = TYPE_CREATING
                    saveGuideDomain(preferences, finalContext.guide)
                    preferences.remove(KEY_OLD_RELATIVE_PATH)
                    preferences.remove(KEY_POSITION)
                }

                is GuideContext.Moving -> {
                    preferences[KEY_CONTEXT_TYPE] = TYPE_MOVING
                    saveGuideDomain(preferences, finalContext.guide)
                    preferences[KEY_OLD_RELATIVE_PATH] =
                        finalContext.oldRelativeGuidePath.value
                    preferences.remove(KEY_POSITION)
                }

                is GuideContext.Browsing -> {
                    preferences[KEY_CONTEXT_TYPE] = TYPE_BROWSING
                    saveGuideDomain(preferences, finalContext.guide)
                    preferences[KEY_POSITION] = finalContext.position
                    preferences.remove(KEY_OLD_RELATIVE_PATH)
                }

                is GuideContext.Editing -> {
                    preferences[KEY_CONTEXT_TYPE] = TYPE_EDITING
                    saveGuideDomain(preferences, finalContext.guide)
                    preferences[KEY_POSITION] = finalContext.position
                    preferences.remove(KEY_OLD_RELATIVE_PATH)
                }

                is GuideContext.DeleteGuide -> {
                    preferences[KEY_CONTEXT_TYPE] = TYPE_DELETE
                    saveGuideDomain(preferences, finalContext.guide)
                    preferences.remove(KEY_OLD_RELATIVE_PATH)
                    preferences.remove(KEY_POSITION)
                }

                is GuideContext.Rename -> {
                    preferences[KEY_CONTEXT_TYPE] = TYPE_EDITING
                    saveGuideDomain(preferences, finalContext.guide)
                    preferences.remove(KEY_OLD_RELATIVE_PATH)
                    preferences.remove(KEY_POSITION)
                }
            }
        }
    }

    private fun saveGuideDomain(preferences: MutablePreferences, guide: GuideDomainModel) {
        preferences[KEY_VERSION] = guide.version.name
        preferences[KEY_NAME] = guide.nameGuide
        preferences[KEY_DESCRIPTION] = guide.description
    }

    override suspend fun clear() {
        preferencesDataStore.edit { preferences ->
            preferences.remove(KEY_CONTEXT_TYPE)
            preferences.remove(KEY_VERSION)
            preferences.remove(KEY_NAME)
            preferences.remove(KEY_DESCRIPTION)
            preferences.remove(KEY_OLD_RELATIVE_PATH)
        }
    }
}