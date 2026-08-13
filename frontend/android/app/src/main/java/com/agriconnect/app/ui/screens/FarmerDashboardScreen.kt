package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    token: String,
    userId: String,
    userName: String,
    viewModel: FarmerViewModel,
    onBookSlotClick: () -> Unit,
    onMyBookingsClick: () -> Unit,
    onMyProductsClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit
) {
    val dashboardData by viewModel.dashboardData
    val loading by viewModel.loading

    LaunchedEffect(token, userId) {
        if (token.isNotEmpty()) {
            viewModel.fetchDashboard(token, userId)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Agriculture, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AgriConnect", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            color = Color.White
                        ) 
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Emerald600
                )
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Emerald600)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Rapido inspired)
                Surface(
                    color = Color.White,
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back,",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${userName.split(" ").firstOrNull() ?: "Farmer"}! 👋",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.WbSunny, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("28°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("WARANGAL", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                    }
                                }
                            }
                            
                            Button(
                                onClick = onBookSlotClick,
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                            ) {
                                Text("Add Produce", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // Error Display
                    val error by viewModel.error
                    if (error != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Target: https://agriconnect-backend-2jig.onrender.com/", color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                                    TextButton(onClick = { viewModel.fetchDashboard(token, userId) }) {
                                        Text("RETRY SYNC", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    AgriSectionTitle(title = "Network Metrics", subtitle = "OPERATIONAL INTELLIGENCE")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val stats = dashboardData?.stats
                        item { 
                            DashboardStatCard(
                                label = "Listings", 
                                value = stats?.totalProducts?.toString() ?: "0", 
                                icon = Icons.Outlined.Inventory2, 
                                bgColor = Color(0xFFEBF5FF), 
                                iconColor = Color(0xFF3B82F6),
                                onClick = onMyProductsClick
                            ) 
                        }
                        item { 
                            DashboardStatCard(
                                label = "Active", 
                                value = stats?.activeProducts?.toString() ?: "0", 
                                icon = Icons.Outlined.PlayCircle, 
                                bgColor = Color(0xFFECFDF5), 
                                iconColor = Emerald600,
                                onClick = onMyProductsClick
                            ) 
                        }
                        item { 
                            DashboardStatCard(
                                label = "Deals", 
                                value = stats?.totalBookings?.toString() ?: "0", 
                                icon = Icons.Outlined.Assignment, 
                                bgColor = Color(0xFFF5F3FF), 
                                iconColor = Color(0xFF8B5CF6),
                                onClick = onMyBookingsClick
                            ) 
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AgriSectionTitle(
                        title = "Recent Crops", 
                        subtitle = "MANAGEMENT",
                        actionText = "See All", 
                        onActionClick = onMyProductsClick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    dashboardData?.recentProducts?.take(3)?.forEach { product ->
                        RecentProductItem(product, onClick = { onProductClick(product.id) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    AgriSectionTitle(
                        title = "Recent Inquiries", 
                        subtitle = "SOURCE REQUESTS",
                        actionText = "View All", 
                        onActionClick = onMyBookingsClick
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (dashboardData?.recentBookings?.isEmpty() == true) {
                        EmptyStateCard(message = "No active inquiries yet. We'll notify you when a buyer is interested in your crops.")
                    } else {
                        dashboardData?.recentBookings?.take(3)?.forEach { booking ->
                            RecentInquiryItem(
                                title = booking["product_name"] as? String ?: "Unknown",
                                buyer = booking["buyer_name"] as? String ?: "Merchant",
                                onCallClick = { /* Call logic */ }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
