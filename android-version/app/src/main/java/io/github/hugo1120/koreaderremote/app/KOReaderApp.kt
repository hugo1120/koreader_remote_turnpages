package io.github.hugo1120.koreaderremote.app

import android.app.Application

class KOReaderApp : Application() {
    val container: AppContainer by lazy {
        AppContainer(this)
    }
}
