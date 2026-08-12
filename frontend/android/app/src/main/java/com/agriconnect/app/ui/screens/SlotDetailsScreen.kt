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
import com.agriconnect.app.ui.theme.Emerald600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotDetailsScreen(slotId: String, onInterestClick: () -> Unit) {
    var isBooked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = { Text("AUDIT NODE", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 2.sp) },
            navigationIcon = {
                IconButton(onClick = { /* Back */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Gray)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Text("INTELLIGENCE IMAGERY", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFF9FAFB)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("FARMER-8422", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Warangal Sourcing Cell", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(color = Color(0xFFECFDF5), shape = RoundedCornerShape(8.dp)) {
                        Text("VERIFIED", color = Emerald600, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("SUPPLY PARAMETERS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem(Modifier.weight(1f), "Produce Label", "Premium Cotton")
                    InfoItem(Modifier.weight(1f), "Inventory Node", "1200 Kg")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem(Modifier.weight(1f), "Market Rate", "₹74 / Kg")
                    InfoItem(Modifier.weight(1f), "Audit Status", "Pass")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem(Modifier.weight(1f), "Registry Group", "Commercial")
                    InfoItem(Modifier.weight(1f), "Harvest Window", "May 2024")
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                // Protocol Info (Like web)
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF9FAFB)).padding(20.dp)) {
                    Column {
                        Text("ANONYMOUS EXCHANGE PROTOCOL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AgriConnect acts as the sole mediator for this node. Personal identities remain protected until fulfillment.", fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
                    }
                }
            }
        }

        // Bottom Actions
        if (!isBooked) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onInterestClick,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text("SIGNAL INTEREST", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                Button(
                    onClick = { isBooked = true },
                    modifier = Modifier.weight(1.2f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("BOOK THIS CROP", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        } else {
            Box(modifier = Modifier.padding(24.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.Black).padding(20.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = Emerald600, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("CROP RESERVED IN REGISTRY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun InfoItem(modifier: Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}
