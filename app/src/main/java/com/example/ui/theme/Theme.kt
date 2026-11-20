package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CotocollaoLightColorScheme = lightColorScheme(
    primary = CotocollaoOrange,
    onPrimary = PureWhite,
    primaryContainer = CotocollaoOrangeContainer,
    onPrimaryContainer = CotocollaoOrangeDark,
    secondary = DarkSlate,
    onSecondary = PureWhite,
    secondaryContainer = SurfaceMuted,
    onSecondaryContainer = DarkSlate,
    tertiary = StatusGreen,
    onTertiary = PureWhite,
    background = SoftSurface,
    onBackground = DarkSlate,
    surface = PureWhite,
    onSurface = DarkSlate,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = SecondarySlate,
    outline = OutlineSubtle,
    error = StatusRed,
    onError = PureWhite,
    errorContainer = StatusRedBg,
    onErrorContainer = StatusRed
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun CotocollaoMonitorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CotocollaoLightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
