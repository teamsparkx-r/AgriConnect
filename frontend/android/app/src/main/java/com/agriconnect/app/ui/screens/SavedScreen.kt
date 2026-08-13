package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agriconnect.app.ui.components.EmptyStateCard
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onProductClick: (String) -> Unit,
    onBack: () -> Unit = {},
    authViewModel: AuthViewModel
) {
    val products = emptyList<String>() 

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Saved Nodes", style = MaterialTheme.typography.titleLarge, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Emerald600)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp), 
            contentAlignment = Alignment.Center
        ) {
            if (products.isEmpty()) {
                EmptyStateCard(message = "Your saved supply nodes will appear here. Track interesting crops by marking them as favorites during discovery.")
            } else {
                // Future list implementation
            }
        }
    }
}
