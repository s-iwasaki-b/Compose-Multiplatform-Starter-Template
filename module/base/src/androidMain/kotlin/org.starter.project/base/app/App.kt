package org.starter.project.base.app

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        private lateinit var instance: App

        fun provideContext(): Context {
            return instance.applicationContext
        }
    }

    init {
        instance = this
    }
}
