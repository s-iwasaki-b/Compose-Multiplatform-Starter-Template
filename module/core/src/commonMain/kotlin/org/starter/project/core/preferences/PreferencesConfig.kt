package org.starter.project.core.preferences

import com.russhwolf.settings.Settings

class PreferencesConfig(factory: Settings.Factory) {
    val zennPreferences: Settings = factory.create("zenn_preferences")
}
