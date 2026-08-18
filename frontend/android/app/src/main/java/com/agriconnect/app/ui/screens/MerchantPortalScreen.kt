package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.app.ui.viewmodel.MerchantViewModel
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import com.agriconnect.data.model.Product
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantPortalScreen(
    token: String,
    userId: String,
    productViewModel: ProductViewModel,
    merchantViewModel: MerchantViewModel = viewModel(),
    authViewModel: AuthViewModel? = null,
    userStatus: String = "active",
    onProductClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    onMyBookingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val products by productViewModel.products
    val productLoading by productViewModel.loading
    val dashboardData by merchantViewModel.dashboardData
    val hasUnread by merchantViewModel.hasUnread

    val categories = listOf("Vegetables", "Fruits", "Grains", "Pulses", "Spices")

    LaunchedEffect(token, userId) {
        if (token.isNotEmpty()) {
            productViewModel.fetchDiscoveryProducts(token)
            merchantViewModel.fetchDashboard(token, userId)
            merchantViewModel.fetchNotifications(token, userId)
        }
    }

    LaunchedEffect(dashboardData) {
        dashboardData?.accountStatus?.let { status ->
            authViewModel?.updateUserStatus(status)
        }
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                hasUnreadNotifications = hasUnread,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onMenuClick = onMenuClick
            )
        }
    ) { padding ->
        if (productLoading && products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Column(modifier = Modifier.padding(24.dp)) {
                        if (userStatus == "pending") {
                            Surface(
                                color = Warning.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Warning.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null, tint = Warning)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        AgriText("Verification Pending", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Gray900)
                                        AgriText("Your account is awaiting Admin approval. You can browse crops but cannot send enquiries until approved.", style = MaterialTheme.typography.bodySmall, color = Gray600)
                                    }
                                }
                            }
                        }

                        AgriText(
                            text = "Market Overview",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500,
                            fontWeight = FontWeight.Medium
                        )
                        AgriText(
                            text = "Hello, Merchant! 👋",
                            style = MaterialTheme.typography.displaySmall,
                            color = Gray900,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                item {
                    // Metrics Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val stats = dashboardData?.summary
                        DashboardStatCard(
                            label = "Active Deals",
                            value = stats?.activeBookings?.toString() ?: "0",
                            icon = Icons.Outlined.Handshake,
                            bgColor = AgriSecondary,
                            iconColor = Success,
                            modifier = Modifier.weight(1f),
                            onClick = onMyBookingsClick
                        )
                        DashboardStatCard(
                            label = "Total Spent",
                            value = "₹${stats?.amountSpent?.toInt() ?: 0}",
                            icon = Icons.Outlined.Payments,
                            bgColor = Info.copy(alpha = 0.1f),
                            iconColor = Info,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        AgriSectionTitle(title = "Browse Categories", subtitle = "DISCOVERY")
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(categories) { cat ->
                                CategoryChip(cat, onClick = onExploreClick)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                if (products.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            AgriSectionTitle(
                                title = "Latest Supply Nodes",
                                subtitle = "LIVE MARKET",
                                actionText = "See All",
                                onActionClick = onExploreClick
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Show 4 products in a 2x2 style (using regular Row for lazy column safety or Grid)
                    items(products.take(4).chunked(2)) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            pair.forEach { product ->
                                MerchantProductCard(
                                    product = product,
                                    onClick = { onProductClick(product.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item { AgriFooter() }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when(label.lowercase()) {
                    "vegetables" -> Icons.Default.Agriculture
                    "fruits" -> Icons.Default.Eco
                    "grains" -> Icons.Default.Grass
                    "pulses" -> Icons.Default.Spa
                    else -> Icons.Default.Category
                },
                contentDescription = null,
                tint = AgriPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            AgriText(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        }
    }
}
