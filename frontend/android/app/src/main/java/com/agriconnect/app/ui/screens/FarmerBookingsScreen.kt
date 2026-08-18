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
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import com.agriconnect.app.ui.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerBookingsScreen(
    onBack: () -> Unit,
    onBookingClick: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    farmerViewModel: FarmerViewModel = viewModel(),
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val token by authViewModel.token
    val user by authViewModel.user
    val bookings by farmerViewModel.bookings
    val loading by farmerViewModel.loading

    var activeTab by remember { mutableStateOf("all") }
    val tabs = listOf(
        TabItem("all", "All"),
        TabItem("active", "Pending"),
        TabItem("completed", "Completed")
    )

    LaunchedEffect(token, user) {
        if (token != null && user != null) {
            farmerViewModel.fetchBookings(token!!, user!!.id)
        }
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Marketplace Inquiries",
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            Surface(
                color = AgriPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = activeTab == tab.id
                        Surface(
                            onClick = { activeTab = tab.id },
                            color = if (isSelected) White else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tab.label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) AgriPrimary else White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            if (loading && bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriPrimary)
                }
            } else if (bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    EmptyStateCard(message = "No active inquiries found.")
                }
            } else {
                val filteredBookings = when (activeTab) {
                    "active" -> bookings.filter { 
                        val s = (it["status"] as? String)?.lowercase() ?: ""
                        s !in listOf("completed", "cancelled", "rejected")
                    }
                    "completed" -> bookings.filter { (it["status"] as? String)?.lowercase() == "completed" }
                    else -> bookings
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    items(filteredBookings) { booking ->
                        val id = booking["booking_id"] as? String ?: ""
                        InquiryCard(
                            crop = booking["product_name"] as? String ?: "Unknown",
                            buyer = booking["buyer_id_alias"] as? String ?: "Anonymous Buyer",
                            status = booking["status"] as? String ?: "Pending",
                            date = (booking["created_at"] as? String)?.take(10) ?: "",
                            location = booking["buyer_district"] as? String ?: "Remote",
                            onClick = { onBookingClick(id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun InquiryCard(crop: String, buyer: String, status: String, date: String, location: String, onClick: () -> Unit) {
    AgriCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val statusColor = when(status.lowercase()) {
                "completed" -> Success
                "confirmed" -> AgriPrimary
                "enquiry_sent", "farmer_responded", "counter_offer", "merchant_responded", "accepted", "pending" -> Warning
                "rejected", "cancelled" -> Error
                else -> Gray500
            }
            val displayStatus = status.replace("_", " ").uppercase()
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = displayStatus,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = date, style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Text(text = crop, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Gray900)
        Text(text = "INTEREST FROM: $buyer", style = MaterialTheme.typography.bodySmall, color = Gray500, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Gray400)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = location, style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Bold)
        }
    }
}

private data class TabItem(val id: String, val label: String)
