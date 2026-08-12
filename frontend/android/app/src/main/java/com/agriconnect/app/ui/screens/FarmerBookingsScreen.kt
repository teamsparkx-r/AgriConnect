package com.agriconnect.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agriconnect.app.ui.components.AgriCard
import com.agriconnect.app.ui.components.AgriSectionTitle
import com.agriconnect.app.ui.components.EmptyStateCard
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import com.agriconnect.app.ui.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerBookingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    farmerViewModel: FarmerViewModel = viewModel()
) {
    val token by authViewModel.token
    val user by authViewModel.user
    val bookings by farmerViewModel.bookings
    val loading by farmerViewModel.loading

    var activeTab by remember { mutableStateOf("all") }
    val tabs = listOf(
        TabItem("all", "All Inquiries"),
        TabItem("active", "Active"),
        TabItem("completed", "Archived")
    )

    LaunchedEffect(token, user) {
        if (token != null && user != null) {
            farmerViewModel.fetchBookings(token!!, user!!.id)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Marketplace Inquiries", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) },
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
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.id == activeTab },
                containerColor = Color.White,
                edgePadding = 24.dp,
                divider = {},
                indicator = {}
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = activeTab == tab.id,
                        onClick = { activeTab = tab.id },
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Surface(
                            color = if (activeTab == tab.id) MaterialTheme.colorScheme.primary else Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = if (activeTab == tab.id) null else BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = tab.label.uppercase(),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (activeTab == tab.id) Color.White else Color.Gray,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            if (loading && bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    EmptyStateCard(message = "No active inquiries found. We'll notify you as soon as a buyer expresses interest in your supply node.")
                }
            } else {
                val filteredBookings = when (activeTab) {
                    "active" -> bookings.filter { (it["status"] as? String)?.lowercase() == "confirmed" }
                    "completed" -> bookings.filter { (it["status"] as? String)?.lowercase() == "completed" }
                    else -> bookings
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    items(filteredBookings) { booking ->
                        InquiryCard(
                            crop = booking["product_name"] as? String ?: "Unknown",
                            buyer = booking["buyer_id_alias"] as? String ?: "Anonymous Buyer",
                            status = booking["status"] as? String ?: "Pending",
                            date = (booking["created_at"] as? String)?.take(10) ?: "",
                            location = booking["buyer_district"] as? String ?: "Remote"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InquiryCard(crop: String, buyer: String, status: String, date: String, location: String) {
    AgriCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = if (status.lowercase() == "confirmed") Color(0xFFECFDF5) else Color(0xFFF3F4F6),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (status.lowercase() == "confirmed") Emerald600 else Color.Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Text(text = crop, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Text(text = "INTEREST FROM: $buyer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = location, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

private data class TabItem(val id: String, val label: String)
