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
    primary = BentoPrimaryDark,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = BentoPrimaryContainer,
    secondary = BentoAmberDark,
    onSecondary = Color(0xFF4E2600),
    secondaryContainer = Color(0xFF6E3900),
    onSecondaryContainer = BentoAmberContainer,
    tertiary = BentoGreenAccent,
    onTertiary = Color(0xFF1B3700),
    background = BentoBackgroundDark,
    surface = BentoSurfaceDark,
    surfaceVariant = BentoSurfaceVariantDark,
    outline = BentoBorderDark,
    outlineVariant = BentoBorderDark.copy(alpha = 0.5f),
    onBackground = BentoTextPrimaryDark,
    onSurface = BentoTextPrimaryDark,
    onSurfaceVariant = BentoTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondary = BentoAmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = BentoAmberContainer,
    onSecondaryContainer = BentoOnAmberContainer,
    tertiary = BentoGreenAccent,
    onTertiary = Color.White,
    tertiaryContainer = BentoGreenContainer,
    onTertiaryContainer = Color(0xFF1B3700),
    background = BentoBackgroundLight,
    surface = BentoSurfaceLight,
    surfaceVariant = BentoSurfaceVariantLight,
    outline = BentoBorderLight,
    outlineVariant = BentoBorderLight.copy(alpha = 0.6f),
    onBackground = BentoTextPrimaryLight,
    onSurface = BentoTextPrimaryLight,
    onSurfaceVariant = BentoTextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve Bento Grid theme colors by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
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

