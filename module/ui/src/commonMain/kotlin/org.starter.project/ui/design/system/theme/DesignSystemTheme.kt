package org.starter.project.ui.design.system.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

object DesignSystemTheme {
    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomTypography.current
}

@Composable
fun DesignSystemTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCustomTypography provides DesignSystemTheme.typography,
        content = content
    )
}

val LocalCustomTypography = staticCompositionLocalOf {
    // TODO: replace your custom typography here
    Typography()
}
