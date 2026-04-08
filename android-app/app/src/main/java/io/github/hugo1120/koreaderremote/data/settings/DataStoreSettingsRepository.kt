package io.github.hugo1120.koreaderremote.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.hugo1120.koreaderremote.data.network.KoreaderHttpClient
import io.github.hugo1120.koreaderremote.domain.model.ControlMode
import io.github.hugo1120.koreaderremote.domain.model.UserPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

interface SettingsRepository {
    val preferencesFlow: Flow<UserPreferences>

    suspend fun rememberSuccessfulConnection(baseUrl: String)

    suspend fun updateDarkTheme(value: Boolean)

    suspend fun updateVolumeKeysEnabled(value: Boolean)

    suspend fun updateInvertVolumeKeys(value: Boolean)

    suspend fun updateLastControlMode(mode: ControlMode)
}

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    constructor(context: Context) : this(context.applicationContext.dataStore)

    private object Keys {
        val lastHost = stringPreferencesKey("last_host")
        val lastPort = intPreferencesKey("last_port")
        val recentHosts = stringPreferencesKey("recent_hosts")
        val preferredSubnetPrefix = stringPreferencesKey("preferred_subnet_prefix")
        val darkTheme = booleanPreferencesKey("dark_theme")
        val volumeKeysEnabled = booleanPreferencesKey("volume_keys_enabled")
        val invertVolumeKeys = booleanPreferencesKey("invert_volume_keys")
        val lastControlMode = stringPreferencesKey("last_control_mode")
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
                val legacyAwareConnection = decodeLegacyConnection(
                    rawLastHost = preferences[Keys.lastHost].orEmpty(),
                    rawLastPort = preferences[Keys.lastPort],
                )
                UserPreferences(
                    lastHost = legacyAwareConnection.host,
                    lastPort = legacyAwareConnection.port,
                    recentHosts = decodeRecentHosts(preferences[Keys.recentHosts]),
                    preferredSubnetPrefix = preferences[Keys.preferredSubnetPrefix].orEmpty(),
                    darkTheme = preferences[Keys.darkTheme] ?: false,
                    volumeKeysEnabled = preferences[Keys.volumeKeysEnabled] ?: true,
                    invertVolumeKeys = preferences[Keys.invertVolumeKeys] ?: false,
                    lastControlMode = decodeControlMode(preferences[Keys.lastControlMode]),
                )
            }

    override suspend fun rememberSuccessfulConnection(baseUrl: String) {
        val endpoint = KoreaderHttpClient.parseHostPort(baseUrl)
        dataStore.edit { preferences ->
            preferences[Keys.lastHost] = endpoint.host
            preferences[Keys.lastPort] = endpoint.port
            val current = decodeRecentHosts(preferences[Keys.recentHosts])
            val updatedRecentHosts = listOf(endpoint.hostPort) +
                current.filterNot { it == endpoint.hostPort }
            preferences[Keys.recentHosts] = encodeRecentHosts(
                updatedRecentHosts.take(MAX_RECENT_HOSTS_COUNT),
            )
            val ipv4Prefix = ipv4PrefixOrNull(endpoint.host)
            if (ipv4Prefix != null) {
                preferences[Keys.preferredSubnetPrefix] = ipv4Prefix
            }
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

    override suspend fun updateLastControlMode(mode: ControlMode) {
        dataStore.edit { preferences ->
            preferences[Keys.lastControlMode] = mode.name
        }
    }

    private fun decodeRecentHosts(encoded: String?): List<String> =
        encoded
            .orEmpty()
            .split(RECENT_HOSTS_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun encodeRecentHosts(hosts: List<String>): String =
        hosts.joinToString(separator = RECENT_HOSTS_SEPARATOR)

    private fun decodeControlMode(value: String?): ControlMode {
        return when (value) {
            ControlMode.Blind.name -> ControlMode.Blind
            else -> ControlMode.Button
        }
    }

    private fun ipv4PrefixOrNull(host: String): String? {
        val segments = host.split('.')
        if (segments.size != 4) return null
        if (segments.any { segment ->
                segment.isEmpty() ||
                    segment.length > 3 ||
                    segment.any { !it.isDigit() } ||
                    segment.toInt() !in 0..255
            }
        ) {
            return null
        }
        return segments.take(3).joinToString(separator = ".")
    }

    private fun decodeLegacyConnection(
        rawLastHost: String,
        rawLastPort: Int?,
    ): KoreaderHttpClient.ParsedHostPort {
        if (rawLastHost.isBlank()) {
            return KoreaderHttpClient.ParsedHostPort(
                host = "",
                port = rawLastPort ?: DEFAULT_KOREADER_PORT,
                hostPort = "",
            )
        }

        if (rawLastPort != null) {
            return KoreaderHttpClient.ParsedHostPort(
                host = rawLastHost,
                port = rawLastPort,
                hostPort = KoreaderHttpClient.formatHostPort(rawLastHost, rawLastPort),
            )
        }

        return runCatching {
            KoreaderHttpClient.parseHostPort(rawLastHost)
        }.getOrElse {
            KoreaderHttpClient.ParsedHostPort(
                host = rawLastHost,
                port = DEFAULT_KOREADER_PORT,
                hostPort = KoreaderHttpClient.formatHostPort(rawLastHost, DEFAULT_KOREADER_PORT),
            )
        }
    }

    companion object {
        private const val DEFAULT_KOREADER_PORT = 8080
        private const val MAX_RECENT_HOSTS_COUNT = 8
        private const val RECENT_HOSTS_SEPARATOR = "\n"
    }
}
