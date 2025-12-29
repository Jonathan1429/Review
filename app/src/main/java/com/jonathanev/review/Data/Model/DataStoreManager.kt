package com.jonathanev.review.Data.Model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "CONTADOR_IMAGENES")
    private val dataStore = context.dataStore

    companion object {
        val contador_imagenes_key = intPreferencesKey("CONTADOR_IMAGENES_KEY")
        val dont_ask_delete_key = booleanPreferencesKey("DONT_ASK_DELETE_KEY")
    }

    fun getCountImage(): Flow<Int> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { pref ->
                pref[contador_imagenes_key] ?: 0
            }
    }

    suspend fun setIncrementCounter() {
        dataStore.edit { pref ->
            val currentCounter = pref[contador_imagenes_key] ?: 0
            pref[contador_imagenes_key] = currentCounter + 1
        }
    }

    suspend fun resetCounter() {
        dataStore.edit { pref ->
            pref[contador_imagenes_key] = 0
        }
    }

    suspend fun setCounter(count: Int){
        dataStore.edit { pref ->
            pref[contador_imagenes_key] = count
        }
    }

    // DeleteQuestions
    suspend fun setDontAskDelete(value: Boolean) {
        dataStore.edit { pref ->
            pref[dont_ask_delete_key] = value
        }
    }

    fun getDontAskDelete(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { pref ->
                pref[dont_ask_delete_key] ?: false
            }
    }
}