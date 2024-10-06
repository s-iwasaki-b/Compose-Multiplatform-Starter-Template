package org.starter.project.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.koin.compose.KoinApplication
import org.starter.project.di.koin
import org.starter.project.feature.home.HomeScreen
import org.starter.project.ui.design.system.theme.DesignSystemTheme

@Composable
fun MainApp() {
    KoinApplication(koin()) {
        DesignSystemTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                HomeScreen()
            }
        }
    }
}