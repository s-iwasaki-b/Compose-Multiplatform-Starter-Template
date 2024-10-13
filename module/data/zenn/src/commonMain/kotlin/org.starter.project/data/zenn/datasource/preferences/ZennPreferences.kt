package org.starter.project.data.zenn.datasource.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.nullableString
import org.starter.project.core.preferences.Preference

class ZennPreferences(
    settings: Settings
) {
    var lastKeyword: String? by settings.nullableString(Preference.LastKeyword.key)
}
