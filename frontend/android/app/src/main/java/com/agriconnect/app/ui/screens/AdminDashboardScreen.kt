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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    token: String,
    onPendingSlotsClick: () -> Unit,
    onCropManagementClick: () -> Unit,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val dashboardData by viewModel.dashboardData
    val loading by viewModel.loading
    val error by viewModel.error
    val hasUnread by viewModel.hasUnread

    LaunchedEffect(key1 = true) {
        viewModel.fetchDashboard(token)
        viewModel.fetchNotifications(token)
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            AgriTopAppBar(
                title = "Admin Panel",
                hasUnreadNotifications = hasUnread,
                onMenuClick = onMenuClick,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
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
            ) {
                // Header Welcome
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Gray900
                    )
                    Text(
                        text = "Welcome back, Admin! Here's what's happening today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }

                // Stats Grid
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatsCard(Modifier.weight(1f), dashboardData?.stats?.totalFarmers?.toString() ?: "0", "Total Farmers", Icons.Outlined.Person, Color(0xFFE3F2FD), Color(0xFF1976D2))
                        StatsCard(Modifier.weight(1f), dashboardData?.stats?.totalMerchants?.toString() ?: "0", "Total Merchants", Icons.Outlined.Storefront, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
                        StatsCard(Modifier.weight(1f), dashboardData?.stats?.activeProducts?.toString() ?: "0", "Active Listings", Icons.Outlined.Agriculture, Color(0xFFE8F5E9), Color(0xFF388E3C))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatsCard(Modifier.weight(1f), dashboardData?.stats?.totalBookings?.toString() ?: "0", "Total Bookings", Icons.Outlined.Assignment, Color(0xFFFDF2F2), Color(0xFFE02424))
                        StatsCard(Modifier.weight(1f), dashboardData?.stats?.totalBookings?.toString() ?: "0", "Total Orders", Icons.Outlined.ShoppingBag, Color(0xFFFFF7ED), Color(0xFFEA580C))
                        StatsCard(Modifier.weight(1f), "₹${(dashboardData?.stats?.totalRevenue ?: 0.0) / 100000.0}L", "Revenue (Est)", Icons.Outlined.Payments, Color(0xFFF0FDFA), Color(0xFF0D9488))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Listings Overview (Mock Chart)
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Listings Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Surface(color = Gray100, shape = RoundedCornerShape(8.dp)) {
                            Text("This Month", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(White, RoundedCornerShape(16.dp)).padding(16.dp)) {
                        // Mock Chart Placeholder
                        Text("LISTINGS TREND CHART", modifier = Modifier.align(Alignment.Center), color = Gray300, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Recent Bookings
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent Bookings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        TextButton(onClick = { }) {
                            Text("View All", color = AgriPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    BookingItem("Red Chillies", "Ravi Kumar", "enquiry_sent", "2026-05-20")
                    BookingItem("Cotton", "Suresh Kumar", "confirmed", "2026-05-19")
                    BookingItem("Maize", "Mahesh Babu", "completed", "2026-05-18")
                }

                AgriFooter()

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun StatsCard(modifier: Modifier, value: String, label: String, icon: ImageVector, bgColor: Color, iconColor: Color) {
    Surface(
        modifier = modifier,
        color = White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 0.5.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(32.dp).background(bgColor, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Gray900)
            Text(label, fontSize = 9.sp, color = Gray500, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookingItem(crop: String, farmer: String, status: String, time: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 0.5.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Gray100, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Agriculture, null, tint = AgriPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(crop, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text("by $farmer", color = Gray500, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.End) {
                val statusColor = when(status.lowercase()) {
                    "completed" -> Success
                    "confirmed" -> AgriPrimary
                    "enquiry_sent", "farmer_responded", "counter_offer", "merchant_responded", "accepted", "pending" -> Warning
                    "rejected", "cancelled" -> Error
                    else -> Gray500
                }
                Text(
                    text = status.replace("_", " ").uppercase(),
                    color = statusColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
                Text(time.take(10), color = Gray400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
