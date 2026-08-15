package com.agriconnect.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.agriconnect.app.ui.components.AgriButton
import com.agriconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFarmScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("My Farm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AgriPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    Text("Farm Details", style = MaterialTheme.typography.labelSmall, color = White, fontWeight = FontWeight.Black)
                    Text("Demo Farm", style = MaterialTheme.typography.headlineMedium, color = White, fontWeight = FontWeight.Black)
                    Text("Warangal, Telangana", style = MaterialTheme.typography.bodySmall, color = White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        FarmInfoRow(Icons.Outlined.Agriculture, "Farm Size", "8 Acres")
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Gray100, thickness = 0.5.dp)
                        FarmInfoRow(Icons.Outlined.Agriculture, "Soil Type", "Black Soil")
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Gray100, thickness = 0.5.dp)
                        FarmInfoRow(Icons.Outlined.Agriculture, "Irrigation", "Drip Irrigation")
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Gray100, thickness = 0.5.dp)
                        FarmInfoRow(Icons.Outlined.Agriculture, "Main Crops", "Cotton, Maize")
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Gray100, thickness = 0.5.dp)
                        FarmInfoRow(Icons.Outlined.Agriculture, "Farming Since", "2018")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AgriButton(
                    text = "Edit Farm Details",
                    onClick = { },
                    containerColor = AgriSecondary,
                    contentColor = AgriPrimary
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun FarmInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = AgriPrimary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Gray500, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Gray900, fontWeight = FontWeight.Black)
    }
}
