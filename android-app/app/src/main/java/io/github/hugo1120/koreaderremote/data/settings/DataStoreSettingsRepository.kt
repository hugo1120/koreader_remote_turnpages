package io.github.hugo1120.koreaderremote.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.hugo1120.koreaderremote.domain.model.UserPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface SettingsRepository {
    val preferencesFlow: Flow<UserPreferences>

    suspend fun updateLastHost(value: String)

    suspend fun updateDarkTheme(value: Boolean)

    suspend fun updateVolumeKeysEnabled(value: Boolean)

    suspend fun updateInvertVolumeKeys(value: Boolean)
}

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    private object Keys {
        val lastHost = stringPreferencesKey("last_host")
        val darkTheme = booleanPreferencesKey("dark_theme")
        val volumeKeysEnabled = booleanPreferencesKey("volume_keys_enabled")
        val invertVolumeKeys = booleanPreferencesKey("invert_volume_keys")
    }

    override val preferencesFlow: Flow<UserPreferences> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserPreferences(
                    lastHost = preferences[Keys.lastHost].orEmpty(),
                    darkTheme = preferences[Keys.darkTheme] ?: false,
                    volumeKeysEnabled = preferences[Keys.volumeKeysEnabled] ?: true,
                    invertVolumeKeys = preferences[Keys.invertVolumeKeys] ?: false,
                )
            }

    override suspend fun updateLastHost(value: String) {
        dataStore.edit { preferences ->
            preferences[Keys.lastHost] = value
        }
    }

    override suspend fun updateDarkTheme(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.darkTheme] = value
        }
    }

    override suspend fun updateVolumeKeysEnabled(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.volumeKeysEnabled] = value
        }
    }

    override suspend fun updateInvertVolumeKeys(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.invertVolumeKeys] = value
        }
    }
}
