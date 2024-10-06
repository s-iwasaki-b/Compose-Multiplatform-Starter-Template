package org.starter.project.core.preferences

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings

internal actual val factory: Settings.Factory = NSUserDefaultsSettings.Factory()
