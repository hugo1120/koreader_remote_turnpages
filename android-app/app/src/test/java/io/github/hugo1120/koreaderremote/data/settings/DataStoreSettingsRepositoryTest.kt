package io.github.hugo1120.koreaderremote.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
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
        assertThat(preferences.darkTheme).isFalse()
        assertThat(preferences.volumeKeysEnabled).isFalse()
        assertThat(preferences.invertVolumeKeys).isFalse()
    }

    @Test
    fun `reads updated values from preferencesFlow`() = runTest {
        val repository = createRepository(fileName = "updates.preferences_pb", scope = backgroundScope)
        repository.updateLastHost("http://192.168.1.88:8080")
        repository.updateDarkTheme(true)
        repository.updateVolumeKeysEnabled(true)

        val preferences = repository.preferencesFlow.first()

        assertThat(preferences.lastHost).isEqualTo("http://192.168.1.88:8080")
        assertThat(preferences.darkTheme).isTrue()
        assertThat(preferences.volumeKeysEnabled).isTrue()
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
        val dataStore = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { temporaryFolder.newFile(fileName) },
        )
        return DataStoreSettingsRepository(dataStore = dataStore)
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
