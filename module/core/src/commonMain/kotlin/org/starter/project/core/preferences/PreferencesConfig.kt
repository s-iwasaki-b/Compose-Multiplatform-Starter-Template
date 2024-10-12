package org.starter.project.core.preferences

import com.russhwolf.settings.Settings

internal expect val factory: Settings.Factory

object PreferencesConfig {
    val zennPreferences: Settings
        get() = factory.create("zenn_preferences")
}
