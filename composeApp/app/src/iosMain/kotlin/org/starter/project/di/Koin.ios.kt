package org.starter.project.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

actual val platformModule = module {
    single<Settings.Factory> { NSUserDefaultsSettings.Factory() }
}
