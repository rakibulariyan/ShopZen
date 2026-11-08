package com.barakwebsoft.shopzen.ui.theme

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
    primary = ShopZenAccent,
    onPrimary = ShopZenDarkBackground,
    primaryContainer = ShopZenPrimaryDark,
    onPrimaryContainer = ShopZenAccent,
    secondary = ShopZenPrimary,
    onSecondary = Color.White,
    background = ShopZenDarkBackground,
    onBackground = ShopZenMainTextDark,
    surface = ShopZenDarkSurface,
    onSurface = ShopZenMainTextDark,
    surfaceVariant = ShopZenDarkSurfaceVariant,
    onSurfaceVariant = ShopZenSecondaryTextDark,
    error = ShopZenError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ShopZenPrimary,
    onPrimary = Color.White,
    primaryContainer = ShopZenAccent,
    onPrimaryContainer = ShopZenPrimaryDark,
    secondary = ShopZenPrimaryDark,
    onSecondary = Color.White,
    background = ShopZenLightBackground,
    onBackground = ShopZenMainTextLight,
    surface = ShopZenLightSurface,
    onSurface = ShopZenMainTextLight,
    surfaceVariant = ShopZenLightSurfaceVariant,
    onSurfaceVariant = ShopZenSecondaryTextLight,
    error = ShopZenError,
    onError = Color.White
)

@Composable
fun ShopZenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Brand identity defaults to ShopZen botanical tokens
    dynamicColor: Boolean = false,
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