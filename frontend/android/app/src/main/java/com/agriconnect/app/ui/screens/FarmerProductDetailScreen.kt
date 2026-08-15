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
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProductDetailScreen(
    productId: String,
    token: String,
    userId: String,
    viewModel: ProductViewModel,
    onEditClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val currentProduct by viewModel.currentProduct
    val loading by viewModel.loading
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        viewModel.fetchProductDetail(productId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Listing?", fontWeight = FontWeight.Black) },
            text = { Text("This action cannot be undone. Your crop will be removed from the marketplace.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProductStatus(token, userId, productId, "removed") {
                            onBack()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Error)
                ) {
                    Text("DELETE", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = Gray500)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = White
        )
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Supply Node Detail",
                showLogo = false,
                onBackClick = onBack
            )
        }
    ) { padding ->
        if (loading && currentProduct == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriPrimary)
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
                        .height(320.dp)
                        .background(Gray100)
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
                            tint = AgriPrimary.copy(alpha = 0.1f)
                        )
                    }
                    
                    // Status Badge
                    val status = product.status ?: "active"
                    val statusColor = when(status.lowercase()) {
                        "active" -> Success
                        "sold" -> Info
                        else -> Warning
                    }
                    Surface(
                        color = statusColor,
                        shape = RoundedCornerShape(bottomStart = 20.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = status.uppercase(),
                            color = White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = product.name ?: "Unknown Crop",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Gray900
                    )
                    Text(
                        text = product.category?.uppercase() ?: "GENERAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = AgriPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AgriSectionTitle(title = "Registry Metadata")
                    Text(
                        text = product.description ?: "Verified supply node in the decentralized marketplace.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoBox(Modifier.weight(1f), "Current Volume", "${product.quantity} ${product.unit}")
                        InfoBox(Modifier.weight(1f), "Market Rate", "₹${product.expectedPrice}/${product.unit}")
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    AgriSectionTitle(title = "Management Actions")
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (product.status?.lowercase() == "active") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AgriButton(
                                text = "MARK SOLD",
                                onClick = { viewModel.updateProductStatus(token, userId, productId, "sold") {} },
                                modifier = Modifier.weight(1f),
                                containerColor = Info
                            )
                            AgriButton(
                                text = "DEACTIVATE",
                                onClick = { viewModel.updateProductStatus(token, userId, productId, "draft") {} },
                                modifier = Modifier.weight(1f),
                                containerColor = Warning
                            )
                        }
                    } else if (product.status?.lowercase() == "sold") {
                        AgriButton(
                            text = "REACTIVATE LISTING",
                            onClick = { viewModel.updateProductStatus(token, userId, productId, "active") {} },
                            containerColor = Success
                        )
                    } else {
                        AgriButton(
                            text = "PUBLISH LISTING",
                            onClick = { viewModel.updateProductStatus(token, userId, productId, "active") {} },
                            containerColor = AgriPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AgriButton(
                            text = "EDIT",
                            onClick = { onEditClick(productId) },
                            modifier = Modifier.weight(1f),
                            containerColor = Gray100,
                            contentColor = Gray900
                        )
                        AgriButton(
                            text = "DELETE",
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            containerColor = Error.copy(alpha = 0.1f),
                            contentColor = Error
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
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
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Black)
            Text(value, style = MaterialTheme.typography.titleMedium, color = Gray900, fontWeight = FontWeight.Black)
        }
    }
}
