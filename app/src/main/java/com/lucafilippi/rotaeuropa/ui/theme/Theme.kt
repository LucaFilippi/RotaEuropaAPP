package com.lucafilippi.rotaeuropa.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8CCEFF),
    onPrimary = Color(0xFF003259),
    primaryContainer = Color(0xFF164E7C),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFD7C36A),
    tertiary = Color(0xFFFAA777),
    background = Color(0xFF0D1726),
    surface = Color(0xFF142237),
    surfaceVariant = Color(0xFF243753),
    onSurface = Color(0xFFE3EAF6),
    onSurfaceVariant = Color(0xFFB8C6DB),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0757A7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E9FF),
    onPrimaryContainer = Color(0xFF0A3157),
    secondary = Color(0xFF745C00),
    tertiary = Color(0xFF9B451D),
    background = Color(0xFFF4F7FC),
    surface = Color(0xFFFFFEFF),
    surfaceVariant = Color(0xFFE6EDF7),
    onSurface = Color(0xFF1A1E25),
    onSurfaceVariant = Color(0xFF515B69),
)

@Composable
fun ROTAEUROPATheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
