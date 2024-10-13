package org.starter.project.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.KoinContext
import org.starter.project.feature.home.HomeScreen
import org.starter.project.ui.design.system.theme.SystemTheme

@Composable
fun MainApp() {
    KoinContext {
        SystemTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SystemTheme.colors.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                HomeScreen()
            }
        }
    }
}
