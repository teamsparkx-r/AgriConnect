package com.agriconnect.app.ui.screens

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
import androidx.compose.ui.draw.clip
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
import com.agriconnect.app.ui.viewmodel.MerchantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    merchantViewModel: MerchantViewModel = viewModel()
) {
    val token by authViewModel.token
    val user by authViewModel.user
    val bookings by merchantViewModel.bookings
    val loading by merchantViewModel.loading

    LaunchedEffect(token, user) {
        if (token != null && user != null) {
            merchantViewModel.fetchBookings(token!!, user!!.id)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("My Orders", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Emerald600)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading && bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald600)
                }
            } else if (bookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    EmptyStateCard(message = "You haven't booked any crops yet. Browse the marketplace to find fresh produce.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    items(bookings) { booking ->
                        OrderCard(
                            crop = booking["product_name"] as? String ?: "Unknown Crop",
                            id = booking["booking_id"] as? String ?: "N/A",
                            status = booking["status"] as? String ?: "Pending",
                            date = (booking["created_at"] as? String)?.take(10) ?: "",
                            farmer = booking["farmer_name"] as? String ?: "Verified Farmer"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(crop: String, id: String, status: String, date: String, farmer: String) {
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
            Text(text = "ID: #$id", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Text(text = crop, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(text = "Sourced from $farmer", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Booked on $date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
