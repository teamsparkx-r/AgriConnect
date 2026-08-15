package com.agriconnect.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AgriPrimary,
    onPrimary = White,
    primaryContainer = AgriSecondary,
    onPrimaryContainer = AgriPrimaryDark,
    
    secondary = AgriPrimaryDark,
    onSecondary = White,
    secondaryContainer = AgriSecondary,
    onSecondaryContainer = AgriPrimaryDark,
    
    background = AgriBackground,
    onBackground = Gray900,
    
    surface = White,
    onSurface = Gray900,
    surfaceVariant = AgriSecondary,
    onSurfaceVariant = Gray600,
    
    outline = Gray200,
    error = Error,
    onError = White
)

@Composable
fun AgriConnectTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AgriBackground.toArgb()
            window.navigationBarColor = AgriPrimary.toArgb()
            
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = false // Dark nav bar needs light icons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
