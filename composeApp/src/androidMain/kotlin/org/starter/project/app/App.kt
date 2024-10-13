package org.starter.project.app

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.starter.project.base.BuildConfig
import org.starter.project.di.startKoin

class App : Application() {
    init {
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
        }
    }
}
