package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriButton
import com.agriconnect.app.ui.theme.Emerald600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantPreferencesScreen(onSave: () -> Unit, onBack: () -> Unit) {
    val locations = listOf("Warangal", "Hanamkonda", "Parkal", "Narsampet", "Jangaon")
    val crops = listOf("Cotton", "Maize", "Paddy", "Red Chilli", "Turmeric")
    
    val selectedLocations = remember { mutableStateListOf<String>() }
    val selectedCrops = remember { mutableStateListOf<String>() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search Preferences", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp)) {
            Text(
                text = "Target Buying Locations",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Text(
                text = "Select regions where you want to discover supply nodes",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(160.dp)
            ) {
                items(locations) { loc ->
                    SelectionChipProper(
                        label = loc,
                        selected = selectedLocations.contains(loc),
                        onToggle = { if (selectedLocations.contains(loc)) selectedLocations.remove(loc) else selectedLocations.add(loc) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Preferred Crop Categories",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Text(
                text = "Personalize your marketplace discovery feed",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(160.dp)
            ) {
                items(crops) { crop ->
                    SelectionChipProper(
                        label = crop,
                        selected = selectedCrops.contains(crop),
                        onToggle = { if (selectedCrops.contains(crop)) selectedCrops.remove(crop) else selectedCrops.add(crop) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AgriButton(
                text = "Save Preferences",
                onClick = onSave
            )
        }
    }
}

@Composable
fun SelectionChipProper(label: String, selected: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.DarkGray
            )
        }
    }
}
