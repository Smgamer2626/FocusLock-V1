package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FocusPrimaryLight,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = FocusPrimaryLight,
    secondary = FocusEmeraldLight,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = FocusEmeraldLight,
    tertiary = FocusAmberLight,
    onTertiary = Slate900,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    error = FocusCrimson,
    onError = PureWhite,
    errorContainer = Slate800,
    onErrorContainer = FocusCrimson
)

private val LightColorScheme = lightColorScheme(
    primary = FocusPrimary,
    onPrimary = PureWhite,
    primaryContainer = FocusPrimaryContainer,
    onPrimaryContainer = FocusOnPrimaryContainer,
    secondary = FocusEmerald,
    onSecondary = PureWhite,
    secondaryContainer = FocusEmeraldContainer,
    onSecondaryContainer = FocusOnEmeraldContainer,
    tertiary = FocusAmber,
    onTertiary = PureWhite,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300,
    error = FocusCrimson,
    onError = PureWhite,
    errorContainer = FocusCrimsonContainer,
    onErrorContainer = FocusOnCrimsonContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent FocusLock branding
    content: @Composable () -> Unit
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
