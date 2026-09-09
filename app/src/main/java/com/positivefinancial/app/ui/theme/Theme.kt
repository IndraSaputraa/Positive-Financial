package com.positivefinancial.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = TealPrimaryLight,
    onPrimary = TealOnPrimaryLight,
    primaryContainer = MintContainerLight,
    onPrimaryContainer = TealOnContainerLight,
    secondary = MintSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = AmberTertiaryLight,
    background = BackgroundLight,
    onBackground = Color(0xFF0D1F1C),
    surface = SurfaceLight,
    onSurface = Color(0xFF0D1F1C),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF3F4947),
    outline = OutlineLight,
    error = ExpenseRed
)

private val DarkColors = darkColorScheme(
    primary = MintPrimaryDark,
    onPrimary = TealOnPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = MintOnContainerDark,
    secondary = MintSecondaryDark,
    tertiary = AmberTertiaryDark,
    background = BackgroundDark,
    onBackground = Color(0xFFE1F3EF),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1F3EF),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC0CDCA),
    outline = OutlineDark,
    error = ExpenseRed
)

@Composable
fun PositiveFinancialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PositiveFinancialTypography,
        content = content
    )
}
