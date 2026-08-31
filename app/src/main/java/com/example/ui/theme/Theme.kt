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
import com.example.data.repository.AppThemeType

// Default Theme (Bento)
private val DarkColorScheme = darkColorScheme(
    primary = BentoPrimaryDark,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = BentoPrimaryContainer,
    secondary = BentoAmberDark,
    background = BentoBackgroundDark,
    surface = BentoSurfaceDark,
    surfaceVariant = BentoSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimary,
    onPrimary = Color.White,
    primaryContainer = BentoPrimaryContainer,
    onPrimaryContainer = BentoOnPrimaryContainer,
    secondary = BentoAmberSecondary,
    background = BentoBackgroundLight,
    surface = BentoSurfaceLight,
    surfaceVariant = BentoSurfaceVariantLight
)

// Ocean Theme
private val OceanLight = lightColorScheme(
    primary = Color(0xFF006C5B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF75F8DF),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4A635E),
    background = Color(0xFFF4FBF9),
    surface = Color(0xFFF4FBF9),
    surfaceVariant = Color(0xFFCCE8E2)
)
private val OceanDark = darkColorScheme(
    primary = Color(0xFF53DBC3),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005144),
    onPrimaryContainer = Color(0xFF75F8DF),
    secondary = Color(0xFFB1CCC5),
    background = Color(0xFF0E1513),
    surface = Color(0xFF0E1513),
    surfaceVariant = Color(0xFF3F4947)
)

// Forest Theme
private val ForestLight = lightColorScheme(
    primary = Color(0xFF386A20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F397),
    onPrimaryContainer = Color(0xFF042100),
    secondary = Color(0xFF55624C),
    background = Color(0xFFFDFDF5),
    surface = Color(0xFFFDFDF5),
    surfaceVariant = Color(0xFFDFE4D7)
)
private val ForestDark = darkColorScheme(
    primary = Color(0xFF9CD67D),
    onPrimary = Color(0xFF0C3900),
    primaryContainer = Color(0xFF1F5107),
    onPrimaryContainer = Color(0xFFB7F397),
    secondary = Color(0xFFBBCBB0),
    background = Color(0xFF1A1C18),
    surface = Color(0xFF1A1C18),
    surfaceVariant = Color(0xFF43483E)
)

// Sunset Theme
private val SunsetLight = lightColorScheme(
    primary = Color(0xFF984061),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFF74565F),
    background = Color(0xFFFFFBFA),
    surface = Color(0xFFFFFBFA),
    surfaceVariant = Color(0xFFF2DDE1)
)
private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    onPrimary = Color(0xFF5E1133),
    primaryContainer = Color(0xFF7B2949),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFE3BDC6),
    background = Color(0xFF201A1B),
    surface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFF514347)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    appThemeType: AppThemeType = AppThemeType.DEFAULT,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        appThemeType == AppThemeType.OCEAN -> if (darkTheme) OceanDark else OceanLight
        appThemeType == AppThemeType.FOREST -> if (darkTheme) ForestDark else ForestLight
        appThemeType == AppThemeType.SUNSET -> if (darkTheme) SunsetDark else SunsetLight
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
