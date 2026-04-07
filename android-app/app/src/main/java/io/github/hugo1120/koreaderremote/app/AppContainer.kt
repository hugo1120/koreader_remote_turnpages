package io.github.hugo1120.koreaderremote.app

import android.content.Context
import io.github.hugo1120.koreaderremote.data.repository.HttpKoreaderRemoteRepository
import io.github.hugo1120.koreaderremote.data.repository.KoreaderRemoteRepository
import io.github.hugo1120.koreaderremote.data.settings.DataStoreSettingsRepository
import io.github.hugo1120.koreaderremote.data.settings.SettingsRepository
import io.github.hugo1120.koreaderremote.platform.input.VolumeKeyActionResolver
import io.github.hugo1120.koreaderremote.platform.storage.MediaStoreScreenshotSaver
import io.github.hugo1120.koreaderremote.platform.storage.ScreenshotSaver
import okhttp3.OkHttpClient

class AppContainer(context: Context) {
    private val okHttpClient = OkHttpClient()

    val settingsRepository: SettingsRepository = DataStoreSettingsRepository(context)
    val remoteRepository: KoreaderRemoteRepository = HttpKoreaderRemoteRepository(okHttpClient)
    val screenshotSaver: ScreenshotSaver = MediaStoreScreenshotSaver(context)
    val volumeKeyActionResolver: VolumeKeyActionResolver = VolumeKeyActionResolver()
}
