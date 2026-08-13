package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    role: String, 
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val user by authViewModel.user
    val scrollState = rememberScrollState()
    var isEditMode by remember { mutableStateOf(false) }
    
    // Edit fields
    var fullName by remember(user) { mutableStateOf(user?.fullName ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var state by remember(user) { mutableStateOf(user?.state ?: "") }
    var district by remember(user) { mutableStateOf(user?.district ?: "") }
    var village by remember(user) { mutableStateOf(user?.village ?: "") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { 
                            val updates = mutableMapOf(
                                "full_name" to fullName,
                                "email" to email,
                                "state" to state,
                                "district" to district
                            )
                            if (role == "farmer") updates["village"] = village
                            
                            authViewModel.updateProfile(updates) { success ->
                                if (success) isEditMode = false
                            }
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Emerald600)
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Profile Card (inspired by Rapido)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user?.fullName?.firstOrNull()?.toString()?.uppercase() ?: "?",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (isEditMode) {
                                AgriTextField(value = fullName, onValueChange = { fullName = it }, label = "Name")
                            } else {
                                Text(text = user?.fullName ?: "Unknown", style = MaterialTheme.typography.titleLarge, color = Color.Black)
                                Text(text = user?.mobile ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(horizontal = 20.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "4.92 My Rating", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                    }
                }
            }

            // Menu Options
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    ProfileListItem(
                        icon = Icons.Outlined.HelpOutline,
                        label = "Help & Support",
                        onClick = { }
                    )
                    DividerItem()
                    ProfileListItem(
                        icon = Icons.Outlined.Payments,
                        label = "Payments",
                        onClick = { }
                    )
                    DividerItem()
                    ProfileListItem(
                        icon = Icons.Outlined.History,
                        label = if (role == "farmer") "My Sales" else "My Orders",
                        onClick = { }
                    )
                    DividerItem()
                    ProfileListItem(
                        icon = Icons.Outlined.Shield,
                        label = "Security & Privacy",
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Actions
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column {
                    ProfileListItem(
                        icon = Icons.Outlined.Logout,
                        label = "Terminate Session",
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = onLogout,
                        showChevron = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DividerItem() {
    Divider(
        modifier = Modifier.padding(start = 72.dp), 
        thickness = 0.5.dp, 
        color = Color.LightGray.copy(alpha = 0.3f)
    )
}
