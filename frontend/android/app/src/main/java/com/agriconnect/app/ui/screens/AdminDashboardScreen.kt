package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agriconnect.app.ui.components.AgriCard
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    token: String,
    onPendingSlotsClick: () -> Unit,
    onCropManagementClick: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val dashboardData by viewModel.dashboardData
    val loading by viewModel.loading
    val error by viewModel.error

    LaunchedEffect(key1 = true) {
        viewModel.fetchDashboard(token)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Control Center", style = MaterialTheme.typography.titleLarge, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Emerald600)
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (error != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CloudOff, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(error!!, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.fetchDashboard(token) }) {
                    Text("RETRY CONNECTION")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = "Network Administration", 
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black
                )
                Text(
                    text = "Manage and audit the decentralized marketplace",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatSmall(Modifier.weight(1f), "Farmers", dashboardData?.stats?.totalFarmers?.toString() ?: "0")
                    AdminStatSmall(Modifier.weight(1f), "Merchants", dashboardData?.stats?.totalMerchants?.toString() ?: "0")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatSmall(Modifier.weight(1f), "Active Crops", dashboardData?.stats?.activeProducts?.toString() ?: "0")
                    AdminStatSmall(Modifier.weight(1f), "Bookings", dashboardData?.stats?.totalBookings?.toString() ?: "0")
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                AdminActionCard(
                    title = "Audit Pending Listings", 
                    subtitle = "Verify and approve new supply nodes", 
                    icon = Icons.Default.Rule, 
                    onClick = onPendingSlotsClick
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AdminActionCard(
                    title = "Manage Global Inventory", 
                    subtitle = "Control category visibility and parameters", 
                    icon = Icons.Default.Inventory2, 
                    onClick = onCropManagementClick
                )
            }
        }
    }
}

@Composable
fun AdminStatSmall(modifier: Modifier, label: String, value: String) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleLarge, color = Color.Black)
        }
    }
}

@Composable
fun AdminActionCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    AgriCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant), 
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Black)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
