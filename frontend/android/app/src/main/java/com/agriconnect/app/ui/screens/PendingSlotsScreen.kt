package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriTopAppBar
import com.agriconnect.app.ui.theme.Emerald600

data class PendingSlot(
    val id: String,
    val farmer: String,
    val crop: String,
    val quantity: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingSlotsScreen(
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val pendingList = listOf(
        PendingSlot("1024", "Ravi Kumar", "Cotton", "800 Kg"),
        PendingSlot("1025", "Suresh", "Maize", "2 Tons")
    )

    Scaffold(
        topBar = {
            AgriTopAppBar(
                title = "Pending Slots",
                showLogo = false,
                onMenuClick = onMenuClick,
                onBackClick = onBack
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pendingList) { slot ->
                PendingSlotCard(slot)
            }
        }
    }
}

@Composable
fun PendingSlotCard(slot: PendingSlot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Booking ID : ${slot.id}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Farmer : ${slot.farmer}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Crop : ${slot.crop}", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text("Quantity : ${slot.quantity}", color = Color.Gray, fontSize = 13.sp)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject", fontSize = 12.sp)
                }
            }
        }
    }
}
