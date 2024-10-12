package org.starter.project.base.app

import android.app.Application
import android.content.Context
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.starter.project.base.BuildConfig

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun provideContext(): Context {
            return instance.applicationContext
        }
    }

    init {
        instance = this
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }
    }
}
