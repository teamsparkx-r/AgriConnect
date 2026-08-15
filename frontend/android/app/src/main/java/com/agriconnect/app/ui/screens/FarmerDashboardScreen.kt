package com.agriconnect.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    token: String,
    userId: String,
    userName: String,
    viewModel: FarmerViewModel,
    userStatus: String = "active",
    onBookSlotClick: () -> Unit,
    onMyBookingsClick: () -> Unit,
    onMyProductsClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val dashboardData by viewModel.dashboardData
    val loading by viewModel.loading

    LaunchedEffect(token, userId) {
        if (token.isNotEmpty()) {
            viewModel.fetchDashboard(token, userId)
        }
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick
            )
        }
    ) { padding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Pending Approval Banner
                if (userStatus == "pending") {
                    Surface(
                        color = Warning.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Warning.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Warning)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Verification Pending", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Gray900)
                                Text("Your account is awaiting Admin approval. You can view listings but cannot list new crops until approved.", style = MaterialTheme.typography.bodySmall, color = Gray600)
                            }
                        }
                    }
                }
                
                // Greeting
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${userName.split(" ").firstOrNull() ?: "Demo"}! 👋",
                        style = MaterialTheme.typography.displayMedium,
                        color = Gray900,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Here's what's happening on your farm today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray400,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Weather & CTA Row
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Weather Card
                    Surface(
                        color = White,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f),
                        shadowElevation = 0.5.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.WbSunny, null, tint = Warning, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("28°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                Text("Warangal, TS", style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Black)
                                Text("Sunny", style = MaterialTheme.typography.labelSmall, color = AgriPrimary, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null, tint = Gray300, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    // Floating Primary CTA
                    val context = androidx.compose.ui.platform.LocalContext.current
                    FloatingActionButton(
                        onClick = {
                            if (userStatus == "pending") {
                                android.widget.Toast.makeText(context, "Account verification pending. Listing restricted.", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                onBookSlotClick()
                            }
                        },
                        containerColor = AgriPrimary,
                        contentColor = White,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.height(70.dp).width(120.dp),
                        elevation = FloatingActionButtonDefaults.elevation(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Produce", style = MaterialTheme.typography.labelLarge, color = White, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Metrics Section
                AgriSectionTitle(title = "Network Metrics", subtitle = "OPERATIONAL INTELLIGENCE")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val stats = dashboardData?.stats
                    item { 
                        DashboardStatCard(
                            label = "Listings", 
                            value = stats?.totalProducts?.toString() ?: "4", 
                            icon = Icons.Outlined.Inventory2, 
                            bgColor = AgriSecondary, 
                            iconColor = AgriPrimary,
                            onClick = onMyProductsClick
                        ) 
                    }
                    item { 
                        DashboardStatCard(
                            label = "Active", 
                            value = stats?.activeProducts?.toString() ?: "4", 
                            icon = Icons.Outlined.PlayCircle, 
                            bgColor = AgriSecondary, 
                            iconColor = Success,
                            onClick = onMyProductsClick
                        ) 
                    }
                    item { 
                        DashboardStatCard(
                            label = "Deals", 
                            value = stats?.totalBookings?.toString() ?: "1", 
                            icon = Icons.Outlined.Assignment, 
                            bgColor = AgriSecondary, 
                            iconColor = Info,
                            onClick = onMyBookingsClick
                        ) 
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Management Section
                AgriSectionTitle(
                    title = "Recent Crops", 
                    subtitle = "MANAGEMENT",
                    actionText = "See All", 
                    onActionClick = onMyProductsClick
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                if (dashboardData?.recentProducts?.isEmpty() == true) {
                    EmptyStateCard(message = "No active listings. Start selling by adding your crops.")
                } else {
                    dashboardData?.recentProducts?.take(3)?.forEach { product ->
                        RecentProductItem(product, onClick = { onProductClick(product.id) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
