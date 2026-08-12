package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriTextField
import com.agriconnect.app.ui.components.EmptyStateCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmersRegistryScreen(onFarmerClick: (String) -> Unit, onBack: () -> Unit) {
    var searchTerm by remember { mutableStateOf("") }
    val farmers = emptyList<String>() 

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Producer Registry", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                AgriTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    label = "Filter registry",
                    placeholder = "Search by name or mobile...",
                    leadingIcon = Icons.Default.Search
                )
            }

            if (farmers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(message = "No matching producer nodes found in the registry. Try adjusting your search filters.")
                }
            } else {
                // Future list implementation
            }
        }
    }
}
