package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriButton
import com.agriconnect.app.ui.components.AgriTextField
import com.agriconnect.app.ui.components.AgriText
import com.agriconnect.app.ui.theme.AgriPrimary
import com.agriconnect.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegistrationScreen(
    role: String, 
    mobile: String, 
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf(mobile) }
    
    // Farmer fields
    var village by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    
    // Merchant fields
    var businessName by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf("") }
    
    // Merchant Preferences
    val preferredLocations = remember { mutableStateListOf<String>() }
    val preferredCrops = remember { mutableStateListOf<String>() }
    
    val locations = listOf("Warangal", "Hanamkonda", "Parkal", "Narsampet", "Jangaon")
    val crops = listOf("Cotton", "Maize", "Paddy", "Red Chilli", "Turmeric")

    val loading by viewModel.loading
    val error by viewModel.error

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { 
                    AgriText(
                        text = if (role == "farmer") "Farmer Registration" else "Merchant Registration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AgriPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AgriText(
                text = "JOIN THE DIRECT SOURCING NETWORK",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            AgriTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name")
            Spacer(modifier = Modifier.height(16.dp))
            
            AgriTextField(value = regMobile, onValueChange = { regMobile = it }, label = "Mobile Number", enabled = mobile.isEmpty())
            Spacer(modifier = Modifier.height(16.dp))
            
            if (role == "farmer") {
                AgriTextField(value = village, onValueChange = { village = it }, label = "Village")
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(value = mandal, onValueChange = { mandal = it }, label = "Mandal")
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(value = district, onValueChange = { district = it }, label = "District")
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(value = state, onValueChange = { state = it }, label = "State")
            } else {
                AgriTextField(value = businessName, onValueChange = { businessName = it }, label = "Business Name")
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(value = currentLocation, onValueChange = { currentLocation = it }, label = "Current Location")
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(value = district, onValueChange = { district = it }, label = "District")
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(value = state, onValueChange = { state = it }, label = "State")
                
                Spacer(modifier = Modifier.height(32.dp))
                
                AgriText(
                    text = "MERCHANT PREFERENCES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                AgriText("Preferred Buying Locations", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Black)
                FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    locations.forEach { loc ->
                        FilterChip(
                            selected = preferredLocations.contains(loc),
                            onClick = { if (preferredLocations.contains(loc)) preferredLocations.remove(loc) else preferredLocations.add(loc) },
                            label = { AgriText(loc, fontWeight = FontWeight.Black) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                AgriText("Preferred Crops", style = MaterialTheme.typography.titleSmall, modifier = Modifier.align(Alignment.Start), fontWeight = FontWeight.Black)
                FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    crops.forEach { crop ->
                        FilterChip(
                            selected = preferredCrops.contains(crop),
                            onClick = { if (preferredCrops.contains(crop)) preferredCrops.remove(crop) else preferredCrops.add(crop) },
                            label = { AgriText(crop, fontWeight = FontWeight.Black) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                AgriText(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start),
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            AgriButton(
                text = "Complete Registration",
                onClick = {
                    viewModel.register(
                        mobileNumber = if (mobile.isNotEmpty()) mobile else regMobile,
                        fullName = fullName,
                        role = role,
                        village = village,
                        mandal = mandal,
                        district = district,
                        state = state,
                        businessName = businessName,
                        currentLocation = currentLocation,
                        preferredLocations = preferredLocations.toList(),
                        preferredCrops = preferredCrops.toList(),
                        onSuccess = onRegistrationSuccess
                    )
                },
                loading = loading,
                enabled = fullName.isNotEmpty() && (mobile.isNotEmpty() || regMobile.length == 10)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
