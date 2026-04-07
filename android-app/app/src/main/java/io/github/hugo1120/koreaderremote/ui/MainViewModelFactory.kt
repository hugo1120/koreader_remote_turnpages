package io.github.hugo1120.koreaderremote.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.hugo1120.koreaderremote.app.AppContainer

class MainViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            "Unknown ViewModel class: $modelClass"
        }
        return MainViewModel(
            remoteRepository = container.remoteRepository,
            settingsRepository = container.settingsRepository,
            screenshotSaver = container.screenshotSaver,
            volumeKeyActionResolver = container.volumeKeyActionResolver,
        ) as T
    }
}
