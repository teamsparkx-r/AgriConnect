package com.agriconnect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import com.agriconnect.app.ui.theme.AgriConnectTheme
import com.agriconnect.app.ui.navigation.AppNavigation
import com.agriconnect.app.ui.screens.MainScreen
import com.agriconnect.app.ui.viewmodel.AuthViewModel

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AgriConnectTheme {
                // Disable system font scaling to keep UI consistent
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(
                        density = density.density,
                        fontScale = 1f
                    )
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    
                    MainScreen(navController = navController, authViewModel = authViewModel) { padding ->
                        AppNavigation(navController, authViewModel, padding)
                    }
                }
            }
        }
    }
}
