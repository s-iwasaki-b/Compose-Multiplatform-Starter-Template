package org.starter.project.core.preferences

import com.russhwolf.settings.Settings

internal expect val factory: Settings.Factory

object PreferencesConfig {
    val accountPreferences: Settings
        get() = factory.create("account_preferences")
}
