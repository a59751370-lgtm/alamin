package com.example.ui.theme

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
    primary = CyberCyan,
    onPrimary = DarkBg,
    primaryContainer = CyberCyanDark,
    onPrimaryContainer = Color.White,
    secondary = CyberTeal,
    onSecondary = DarkBg,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = CyberTeal,
    tertiary = CyberAmber,
    background = DarkBg,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkSurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyberCyanDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = CyberTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF0F766E),
    tertiary = CyberAmber,
    background = LightBg,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF64748B),
    outline = LightSurfaceBorder
)

val IncognitoColorScheme = darkColorScheme(
    primary = IncognitoAccent,
    onPrimary = Color.White,
    primaryContainer = IncognitoAccentLight,
    onPrimaryContainer = IncognitoBg,
    secondary = IncognitoAccentLight,
    onSecondary = IncognitoBg,
    background = IncognitoBg,
    onBackground = Color(0xFFF3E8FF),
    surface = IncognitoSurface,
    onSurface = Color(0xFFF3E8FF),
    surfaceVariant = Color(0xFF2C1E45),
    onSurfaceVariant = Color(0xFFD8B4FE),
    outline = Color(0xFF4A3468)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isIncognito: Boolean = false,
    dynamicColor: Boolean = false, // Use our handcrafted sleek Cyber browser palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isIncognito -> IncognitoColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
