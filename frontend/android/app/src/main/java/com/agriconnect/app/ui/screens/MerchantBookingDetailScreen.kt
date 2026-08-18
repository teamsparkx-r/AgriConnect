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
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.MerchantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantBookingDetailScreen(
    bookingId: String,
    token: String,
    userId: String,
    viewModel: MerchantViewModel,
    onBack: () -> Unit
) {
    val bookingData by viewModel.currentBooking
    val loading by viewModel.loading

    var showCounterDialog by remember { mutableStateOf(false) }
    var counterQty by remember { mutableStateOf("") }
    var counterPrice by remember { mutableStateOf("") }
    var counterMessage by remember { mutableStateOf("") }

    LaunchedEffect(bookingId) {
        viewModel.fetchBookingDetail(token, bookingId, userId)
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Order Details",
                showLogo = false,
                onBackClick = onBack
            )
        }
    ) { padding ->
        if (loading && bookingData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriPrimary)
            }
        } else if (bookingData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Information unavailable.", fontWeight = FontWeight.Black)
            }
        } else {
            val booking = (bookingData?.get("booking") as? Map<String, Any>) ?: emptyMap()
            val status = (booking["status"] as? String) ?: "pending"
            val unit = (booking["product_unit"] as? String) ?: "kg"
            
            val reqQty = (booking["requested_quantity"] as? Number)?.toFloat() 
                ?: (booking["product_quantity"] as? Number)?.toFloat() 
                ?: (booking["quantity"] as? Number)?.toFloat() 
                ?: 0f
                
            val negPrice = (booking["negotiated_price"] as? Number)?.toFloat() 
                ?: (booking["expected_price"] as? Number)?.toFloat() 
                ?: 0f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Farmer Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AgriSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = AgriPrimary)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = booking["farmer_name"] as? String ?: "VERIFIED FARMER",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Gray900
                        )
                        Text(
                            text = "${booking["farmer_village"] ?: "Remote"} Source",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray500,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                AgriSectionTitle(title = "NEGOTIATION TERMS")
                AgriCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = booking["product_name"] as? String ?: "Unknown Crop",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Gray900
                                )
                                Text(
                                    text = "Proposed Quantity: $reqQty $unit",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray500,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${negPrice.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = AgriPrimary
                                )
                                Text(
                                    text = "/ $unit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Gray400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        if (status == "counter_offer") {
                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = Gray100, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                AgriButton(
                                    text = "ACCEPT",
                                    onClick = { viewModel.acceptOffer(token, bookingId, userId) { } },
                                    modifier = Modifier.weight(1f),
                                    containerColor = Success,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                AgriButton(
                                    text = "REJECT",
                                    onClick = { viewModel.rejectOffer(token, bookingId, userId) { } },
                                    modifier = Modifier.weight(1f),
                                    containerColor = Error.copy(alpha = 0.1f),
                                    contentColor = Error,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedButton(
                                    onClick = { 
                                        counterQty = reqQty.toString()
                                        counterPrice = negPrice.toString()
                                        showCounterDialog = true 
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AgriPrimary)
                                ) {
                                    AgriText("COUNTER", fontWeight = FontWeight.Black, color = AgriPrimary, fontSize = 12.sp)
                                }
                            }
                        } else if (status == "enquiry_sent" || status == "merchant_responded") {
                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = Gray100, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            AgriButton(
                                text = "CANCEL ENQUIRY",
                                onClick = { viewModel.rejectOffer(token, bookingId, userId) { } },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Error.copy(alpha = 0.1f),
                                contentColor = Error,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AgriText(
                                text = "AWAITING FARMER RESPONSE",
                                style = MaterialTheme.typography.labelSmall, 
                                color = Warning, 
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AgriSectionTitle(title = "FULFILLMENT STATUS")
                Surface(
                    color = White,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        StatusRow("Order ID", "#${bookingId.take(8).uppercase()}")
                        Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Gray100)
                        
                        val statusColor = when(status.lowercase()) {
                            "completed" -> Success
                            "confirmed" -> AgriPrimary
                            "enquiry_sent", "farmer_responded", "counter_offer", "merchant_responded", "accepted", "pending" -> Warning
                            "rejected", "cancelled" -> Error
                            else -> Gray500
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Current Status", style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Black)
                            Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = status.replace("_", " ").uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                        StatusRow("Booked On", (booking["created_at"] as? String)?.take(10) ?: "N/A")
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                if (status == "confirmed" || status == "accepted") {
                    Button(
                        onClick = { /* Call Action */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gray900)
                    ) {
                        Icon(Icons.Default.Call, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("CONTACT FARMER", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }

    if (showCounterDialog) {
        val booking = (bookingData?.get("booking") as? Map<String, Any>) ?: emptyMap()
        val unit = (booking["product_unit"] as? String) ?: "kg"
        
        AlertDialog(
            onDismissRequest = { showCounterDialog = false },
            title = { Text("Send Counter Offer", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AgriTextField(
                        value = counterQty,
                        onValueChange = { counterQty = it },
                        label = "Quantity ($unit)",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    AgriTextField(
                        value = counterPrice,
                        onValueChange = { counterPrice = it },
                        label = "Counter Price (₹ / $unit)",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    AgriTextField(
                        value = counterMessage,
                        onValueChange = { counterMessage = it },
                        label = "Message to Farmer",
                        placeholder = "e.g. Can we meet halfway?"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = counterQty.toFloatOrNull() ?: 0f
                        val price = counterPrice.toFloatOrNull() ?: 0f
                        if (qty > 0 && price > 0) {
                            viewModel.counterOffer(token, bookingId, userId, qty, price, counterMessage) {
                                showCounterDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary)
                ) {
                    Text("SEND COUNTER", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCounterDialog = false }) {
                    Text("CANCEL", color = Gray500, fontWeight = FontWeight.Black)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
