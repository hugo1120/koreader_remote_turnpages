package io.github.hugo1120.koreaderremote.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import io.github.hugo1120.koreaderremote.domain.model.ControlMode
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `reads default values from preferencesFlow`() = runTest {
        val repository = createRepository(fileName = "defaults.preferences_pb", scope = backgroundScope)

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.lastHost).isEmpty()
        assertThat(preferences.lastPort).isEqualTo(8080)
        assertThat(preferences.recentHosts).isEmpty()
        assertThat(preferences.preferredSubnetPrefix).isEmpty()
        assertThat(preferences.darkTheme).isFalse()
        assertThat(preferences.volumeKeysEnabled).isTrue()
        assertThat(preferences.invertVolumeKeys).isFalse()
        assertThat(preferences.lastControlMode).isEqualTo(ControlMode.Button)
    }

    @Test
    fun `rememberSuccessfulConnection updates host port and ipv4 prefix`() = runTest {
        val repository = createRepository(fileName = "updates.preferences_pb", scope = backgroundScope)
        repository.rememberSuccessfulConnection("http://192.168.1.88:18080")
        repository.updateDarkTheme(true)
        repository.updateVolumeKeysEnabled(true)

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.lastHost).isEqualTo("192.168.1.88")
        assertThat(preferences.lastPort).isEqualTo(18080)
        assertThat(preferences.recentHosts).containsExactly("192.168.1.88:18080")
        assertThat(preferences.preferredSubnetPrefix).isEqualTo("192.168.1")
        assertThat(preferences.darkTheme).isTrue()
        assertThat(preferences.volumeKeysEnabled).isTrue()
    }

    @Test
    fun `rememberSuccessfulConnection keeps recentHosts deduplicated and most recent first`() = runTest {
        val repository = createRepository(fileName = "recent_hosts.preferences_pb", scope = backgroundScope)

        repository.rememberSuccessfulConnection("http://192.168.1.1:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.2:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.3:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.4:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.5:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.6:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.7:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.8:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.9:8080")
        repository.rememberSuccessfulConnection("http://192.168.1.6:8080")

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.recentHosts).containsExactly(
            "192.168.1.6:8080",
            "192.168.1.9:8080",
            "192.168.1.8:8080",
            "192.168.1.7:8080",
            "192.168.1.5:8080",
            "192.168.1.4:8080",
            "192.168.1.3:8080",
            "192.168.1.2:8080",
        ).inOrder()
    }

    @Test
    fun `rememberSuccessfulConnection keeps existing prefix when host is not ipv4`() = runTest {
        val repository = createRepository(fileName = "prefix.preferences_pb", scope = backgroundScope)

        repository.rememberSuccessfulConnection("http://192.168.10.88:8080")
        repository.rememberSuccessfulConnection("http://reader.local:8080")

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.lastHost).isEqualTo("reader.local")
        assertThat(preferences.lastPort).isEqualTo(8080)
        assertThat(preferences.preferredSubnetPrefix).isEqualTo("192.168.10")
        assertThat(preferences.recentHosts.first()).isEqualTo("reader.local:8080")
    }

    @Test
    fun `reads legacy lastHost with custom port into split host and port`() = runTest {
        val dataStore = createDataStore(fileName = "legacy_custom_port.preferences_pb", scope = backgroundScope)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("last_host")] = "192.168.1.88:18080"
        }
        val repository = DataStoreSettingsRepository(dataStore = dataStore)

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.lastHost).isEqualTo("192.168.1.88")
        assertThat(preferences.lastPort).isEqualTo(18080)
    }

    @Test
    fun `reads legacy lastHost with scheme into split host and default port`() = runTest {
        val dataStore = createDataStore(fileName = "legacy_scheme.preferences_pb", scope = backgroundScope)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("last_host")] = "http://192.168.1.88"
        }
        val repository = DataStoreSettingsRepository(dataStore = dataStore)

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.lastHost).isEqualTo("192.168.1.88")
        assertThat(preferences.lastPort).isEqualTo(8080)
    }

    @Test
    fun `updateLastControlMode persists and restores`() = runTest {
        val dataStore = createDataStore(fileName = "control_mode.preferences_pb", scope = backgroundScope)
        val repository = DataStoreSettingsRepository(dataStore = dataStore)

        repository.updateLastControlMode(ControlMode.Blind)
        val persistedValue = dataStore.data.first()[stringPreferencesKey("last_control_mode")]
        val preferences = repository.preferencesFlow.first()

        assertThat(persistedValue).isEqualTo(ControlMode.Blind.name)
        assertThat(preferences.lastControlMode).isEqualTo(ControlMode.Blind)
    }

    @Test
    fun `last_control_mode 存储未知值时回退为 Button`() = runTest {
        val dataStore = createDataStore(fileName = "unknown_control_mode.preferences_pb", scope = backgroundScope)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("last_control_mode")] = "UnknownMode"
        }
        val repository = DataStoreSettingsRepository(dataStore = dataStore)

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.lastControlMode).isEqualTo(ControlMode.Button)
    }

    @Test
    fun `emits default values when datastore throws IOException`() = runTest {
        val repository = DataStoreSettingsRepository(
            dataStore = ThrowingDataStore(IOException("read failed")),
        )

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences).isEqualTo(io.github.hugo1120.koreaderremote.domain.model.UserPreferences())
    }

    @Test
    fun `rethrows non IOException from datastore`() {
        val repository = DataStoreSettingsRepository(
            dataStore = ThrowingDataStore(IllegalStateException("boom")),
        )

        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                repository.preferencesFlow.first()
            }
        }

        assertThat(error).hasMessageThat().contains("boom")
    }

    private fun createRepository(
        fileName: String,
        scope: CoroutineScope,
    ): DataStoreSettingsRepository {
        val dataStore = createDataStore(fileName = fileName, scope = scope)
        return DataStoreSettingsRepository(dataStore = dataStore)
    }

    private fun createDataStore(
        fileName: String,
        scope: CoroutineScope,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { temporaryFolder.newFile(fileName) },
        )
    }

    private class ThrowingDataStore(
        throwable: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw throwable }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return transform(emptyPreferences())
        }
    }
}
