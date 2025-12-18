package com.spectrum.phoenix.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FocusBlue,
    secondary = PhoenixGreen,
    tertiary = PhoenixRed,
    background = Color.Black,
    surface = LightDarkGray,
    error = PhoenixRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    errorContainer = PhoenixRed.copy(alpha = 0.2f)
)

private val LightColorScheme = lightColorScheme(
    primary = FocusBlue,
    secondary = PhoenixGreen,
    tertiary = PhoenixRed,
    background = PhoenixSurface,
    surface = Color.White,
    error = PhoenixRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    errorContainer = PhoenixRed.copy(alpha = 0.1f)
)

@Composable
fun PhoenixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado para mantener nuestra paleta
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
