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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFarmScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "My Farm",
                showLogo = false,
                onBackClick = onBack
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
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=800")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                    AgriText("Farm Details", style = MaterialTheme.typography.labelSmall, color = White, fontWeight = FontWeight.Black)
                    AgriText("Demo Farm", style = MaterialTheme.typography.headlineMedium, color = White, fontWeight = FontWeight.Black)
                    AgriText("Warangal, Telangana", style = MaterialTheme.typography.bodySmall, color = White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
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
                
                AgriFooter()
                
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
        AgriText(label, style = MaterialTheme.typography.bodyMedium, color = Gray500, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        AgriText(value, style = MaterialTheme.typography.bodyLarge, color = Gray900, fontWeight = FontWeight.Black)
    }
}
