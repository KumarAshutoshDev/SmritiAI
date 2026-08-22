package com.teamchromium.smritiai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PatientColorScheme: ColorScheme = lightColorScheme(
    primary = SmritiPrimary,
    onPrimary = SmritiOnPrimary,
    primaryContainer = SmritiInkDeep,
    onPrimaryContainer = SmritiOnPrimary,
    secondary = SmritiSecondary,
    onSecondary = SmritiOnSecondary,
    secondaryContainer = SmritiSurfaceAlt,
    onSecondaryContainer = SmritiInkDeep,
    background = SmritiSurface,
    onBackground = SmritiOnSurface,
    surface = SmritiSurface,
    onSurface = SmritiOnSurface,
    surfaceVariant = SmritiSurfaceAlt,
    onSurfaceVariant = SmritiOnSurface,
    outline = SmritiOutline,
    error = SmritiError,
    onError = SmritiOnError,
)

@Composable
fun SmritiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PatientColorScheme,
        typography = PatientTypography,
        content = content,
    )
}
