package org.starter.project.ui.design.system.theme

import androidx.compose.material.Colors
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

object SystemTheme {
    val colors: Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColors.current
    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomTypography.current
    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomShapes.current
}

@Composable
fun SystemTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCustomColors provides SystemTheme.colors,
        LocalCustomTypography provides SystemTheme.typography,
        LocalCustomShapes provides SystemTheme.shapes,
        content = content
    )
}

val LocalCustomColors = staticCompositionLocalOf {
    // TODO: replace material theme colors with your custom theme colors
    lightColors()
}
val LocalCustomTypography = staticCompositionLocalOf {
    // TODO: replace material theme typography with your custom typography
    Typography()
}
val LocalCustomShapes = staticCompositionLocalOf {
    // TODO: replace material theme shapes with your custom shapes
    Shapes()
}
