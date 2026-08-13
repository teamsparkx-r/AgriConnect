package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.agriconnect.app.ui.components.AgriButton
import com.agriconnect.app.ui.components.AgriSectionTitle
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProductDetailScreen(
    productId: String,
    token: String,
    viewModel: ProductViewModel,
    onEditClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentProduct by viewModel.currentProduct
    val loading by viewModel.loading

    LaunchedEffect(productId) {
        viewModel.fetchProductDetail(productId)
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Supply Node Detail", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(productId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Emerald600)
            )
        }
    ) { padding ->
        if (loading && currentProduct == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Emerald600)
            }
        } else if (currentProduct == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Node unavailable.", fontWeight = FontWeight.Black)
            }
        } else {
            val product = currentProduct!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (product.images != null && (product.images!!.startsWith("http") || product.images!!.startsWith("content"))) {
                        AsyncImage(
                            model = product.images,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Agriculture, 
                            null, 
                            modifier = Modifier.size(100.dp).align(Alignment.Center), 
                            tint = Emerald600.copy(alpha = 0.2f)
                        )
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = product.name ?: "Unknown Crop",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = product.category?.uppercase() ?: "GENERAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = Emerald600,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AgriSectionTitle(title = "Registry Metadata")
                    Text(
                        text = product.description ?: "Verified supply node in the decentralized marketplace.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoBox(Modifier.weight(1f), "Current Volume", "${product.quantity} ${product.unit}")
                        InfoBox(Modifier.weight(1f), "Market Rate", "₹${product.expectedPrice}/${product.unit}")
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    AgriButton(
                        text = "UPDATE REGISTRY DATA",
                        onClick = { onEditClick(productId) },
                        containerColor = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBox(modifier: Modifier, label: String, value: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Black)
            Text(value, style = MaterialTheme.typography.titleMedium, color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}
