package org.starter.project.core.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.starter.project.base.app.App

internal actual val factory: Settings.Factory = SharedPreferencesSettings.Factory(App.provideContext())
