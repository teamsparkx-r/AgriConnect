package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriCard
import com.agriconnect.app.ui.components.AgriSectionTitle
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.FarmerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerBookingDetailScreen(
    bookingId: String,
    token: String,
    userId: String,
    viewModel: FarmerViewModel,
    onBack: () -> Unit
) {
    val bookingData by viewModel.currentBooking
    val loading by viewModel.loading

    LaunchedEffect(bookingId) {
        viewModel.fetchBookingDetail(token, bookingId, userId)
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Inquiry Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Emerald600)
            )
        }
    ) { padding ->
        if (loading && bookingData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Emerald600)
            }
        } else if (bookingData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Information unavailable.", fontWeight = FontWeight.Black)
            }
        } else {
            val booking = (bookingData?.get("booking") as? Map<String, Any>) ?: emptyMap()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Merchant Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = Emerald600)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = booking["buyer_id_alias"] as? String ?: "VERIFIED MERCHANT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Text(
                            text = "${booking["buyer_district"] ?: "Unknown"} District",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                AgriSectionTitle(title = "SECURED ITEM")
                AgriCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = booking["product_name"] as? String ?: "Unknown Crop",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "${booking["product_quantity"] ?: "0"} ${booking["product_unit"] ?: "units"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = "₹${(booking["expected_price"] as? Number)?.toInt() ?: 0}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Emerald600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AgriSectionTitle(title = "FULFILLMENT STATUS")
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        StatusRow("Reservation ID", "#${bookingId.take(8).uppercase()}")
                        Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        StatusRow("Registry Status", (booking["status"] as? String)?.uppercase() ?: "PENDING")
                        Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        StatusRow("Creation Date", (booking["created_at"] as? String)?.take(10) ?: "N/A")
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { /* Call Action */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Call, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("CONTACT MERCHANT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Black)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Black)
    }
}
