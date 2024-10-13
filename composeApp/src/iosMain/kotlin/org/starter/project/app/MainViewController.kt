package org.starter.project.app

import androidx.compose.ui.window.ComposeUIViewController
import org.starter.project.di.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = { startKoin() }
) { MainApp() }
