package org.starter.project.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<Settings.Factory> { SharedPreferencesSettings.Factory(androidContext()) }
}
