package com.agriconnect.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.data.model.Product

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun RecentProductItem(product: Product, onClick: (() -> Unit)? = null) {
    AgriCard(onClick = onClick, modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.images != null && (product.images!!.startsWith("http") || product.images!!.startsWith("content"))) {
                    AsyncImage(
                        model = product.images,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = when(product.category?.lowercase() ?: "other") {
                            "vegetables" -> Icons.Default.Agriculture
                            "fruits" -> Icons.Default.Eco
                            "grains" -> Icons.Default.Grass
                            else -> Icons.Default.Inventory
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name ?: "Unknown Crop",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${product.quantity ?: 0f} ${product.unit ?: "units"} • ₹${product.expectedPrice?.toInt() ?: 0}/${product.unit ?: "unit"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            val status = product.status ?: "active"
            Surface(
                color = if (status.lowercase() == "active") Color(0xFFECFDF5) else Color(0xFFFFF7ED),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (status.lowercase() == "active") Emerald600 else Color(0xFFF59E0B)
                )
            }
        }
    }
}

@Composable
fun RecentInquiryItem(title: String, buyer: String, status: String = "Pending", onCallClick: () -> Unit = {}) {
    AgriCard(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEBF5FF)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.Black, fontWeight = FontWeight.Black)
                Text("BUYER: $buyer", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Button(
                onClick = onCallClick,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SlotCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.images != null && (product.images!!.startsWith("http") || product.images!!.startsWith("content"))) {
                    AsyncImage(
                        model = product.images,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = when(product.category?.lowercase() ?: "other") {
                            "vegetables" -> Icons.Default.Agriculture
                            "fruits" -> Icons.Default.Eco
                            "grains" -> Icons.Default.Grass
                            else -> Icons.Default.Inventory
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(70.dp)
                    )
                }
                Text(
                    text = "VISUAL NODE VERIFIED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (product.images != null) Color.White else Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "FARMER-${product.farmerIdAlias ?: product.farmerId?.takeLast(4) ?: "NODE"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${product.village ?: "Remote"}, ${product.district ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val status = product.status ?: "active"
                    Surface(
                        color = if (status.lowercase() == "active") Color(0xFFECFDF5) else Color(0xFFFFF7ED),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = status.uppercase(),
                            color = if (status.lowercase() == "active") Emerald600 else Color(0xFFF59E0B),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("CROP", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(product.name ?: "Unknown", style = MaterialTheme.typography.titleLarge, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("EXPECTED PRICE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("₹${product.expectedPrice ?: "N/A"}", style = MaterialTheme.typography.titleLarge, color = Emerald600, fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AgriButton(
                    text = "View Market Details",
                    onClick = onClick,
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
