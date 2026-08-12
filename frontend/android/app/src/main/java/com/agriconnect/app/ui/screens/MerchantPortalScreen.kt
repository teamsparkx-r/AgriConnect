package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
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
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.app.ui.viewmodel.MerchantViewModel
import com.agriconnect.data.model.Product
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantPortalScreen(
    token: String,
    userId: String,
    productViewModel: ProductViewModel,
    merchantViewModel: MerchantViewModel = viewModel(),
    onProductClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    onMyBookingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val products by productViewModel.products
    val productLoading by productViewModel.loading
    val dashboardData by merchantViewModel.dashboardData

    val categories = listOf("Vegetables", "Fruits", "Grains", "Pulses", "Spices")

    LaunchedEffect(token, userId) {
        if (token.isNotEmpty()) {
            productViewModel.fetchDiscoveryProducts(token)
            merchantViewModel.fetchDashboard(token, userId)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Agriculture, null, tint = Emerald600, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AgriConnect", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        ) 
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Outlined.Notifications, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        if (productLoading && products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Emerald600)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    // Hero Banner (RockTek Style)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=1000",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Surface(
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("India's verified farm marketplace", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "FIELD TO MARKET.",
                                color = Color.White,
                                style = MaterialTheme.typography.displayLarge,
                                lineHeight = 38.sp
                            )
                            Text(
                                text = "MEDIATED. VERIFIED.",
                                color = Emerald600,
                                style = MaterialTheme.typography.displayLarge,
                                lineHeight = 38.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Source inventory directly from approved farmers across India. Zero mediator fees.",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 22.sp
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Floating Search (Rapido/RockTek Style)
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Search crops, regions...", 
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.LightGray
                                    )
                                    Button(
                                        onClick = onExploreClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(44.dp)
                                    ) {
                                        Text("Search", fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // Error Display
                val merchantError by merchantViewModel.error
                val productError by productViewModel.error
                
                if (merchantError != null || productError != null) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = merchantError ?: productError ?: "Sync error",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Target: http://192.168.1.45:8000/",
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(24.dp)) {
                        AgriSectionTitle(
                            title = "Browse By Category",
                            subtitle = "CATEGORIES"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(categories) { cat ->
                                CategoryChip(cat, onClick = onExploreClick)
                            }
                        }
                    }
                }

                // Market Pulse
                dashboardData?.summary?.let { summary ->
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            AgriSectionTitle(
                                title = "Network Pulse",
                                subtitle = "MARKET ACTIVITY"
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                item { 
                                    DashboardStatCard(
                                        label = "Procured", 
                                        value = summary.totalBookings.toString(), 
                                        icon = Icons.Outlined.ShoppingBag, 
                                        bgColor = Color(0xFFEBF5FF), 
                                        iconColor = Color(0xFF3B82F6),
                                        onClick = onMyBookingsClick
                                    ) 
                                }
                                item { 
                                    DashboardStatCard(
                                        label = "In Transit", 
                                        value = summary.activeBookings.toString(), 
                                        icon = Icons.Outlined.LocalShipping, 
                                        bgColor = Color(0xFFECFDF5), 
                                        iconColor = Emerald600,
                                        onClick = onMyBookingsClick
                                    ) 
                                }
                                item { 
                                    DashboardStatCard(
                                        label = "Investment", 
                                        value = "₹${summary.amountSpent.toInt()}", 
                                        icon = Icons.Outlined.Payments, 
                                        bgColor = Color(0xFFFFF7ED), 
                                        iconColor = Color(0xFFF59E0B),
                                        onClick = onMyBookingsClick
                                    ) 
                                }
                            }
                        }
                    }
                }

                // Featured Supply Nodes
                if (products.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(24.dp)) {
                            AgriSectionTitle(
                                title = "Latest Arrivals",
                                subtitle = "LIVE SUPPLY",
                                actionText = "See All",
                                onActionClick = onExploreClick
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            products.take(5).forEach { product ->
                                SlotCard(product, onClick = { onProductClick(product.id) })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
