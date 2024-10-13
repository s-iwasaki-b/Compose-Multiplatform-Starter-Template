package org.starter.project.data.zenn.datasource.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.nullableString
import org.starter.project.core.preferences.Preference

interface ZennPreferences {
    var lastKeyword: String?
}

class ZennPreferencesImpl(settings: Settings) : ZennPreferences {
    override var lastKeyword: String? by settings.nullableString(Preference.LastKeyword.key)
}
