package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GeoDarkPrimary,
    onPrimary = GeoDarkOnPrimary,
    primaryContainer = GeoDarkPrimaryContainer,
    onPrimaryContainer = GeoDarkOnPrimaryContainer,
    secondary = GeoDarkSecondary,
    onSecondary = GeoDarkOnSecondary,
    secondaryContainer = GeoDarkSecondaryContainer,
    onSecondaryContainer = GeoDarkOnSecondaryContainer,
    background = GeoDarkBackground,
    onBackground = GeoDarkOnBackground,
    surface = GeoDarkSurface,
    onSurface = GeoDarkOnSurface,
    surfaceVariant = GeoDarkSurfaceVariant,
    onSurfaceVariant = GeoDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = GeoPrimary,
    onPrimary = GeoOnPrimary,
    primaryContainer = GeoPrimaryContainer,
    onPrimaryContainer = GeoOnPrimaryContainer,
    secondary = GeoSecondary,
    onSecondary = GeoOnSecondary,
    secondaryContainer = GeoSecondaryContainer,
    onSecondaryContainer = GeoOnSecondaryContainer,
    background = GeoBackground,
    onBackground = GeoOnBackground,
    surface = GeoSurface,
    onSurface = GeoOnSurface,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = GeoOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
