package com.agriconnect.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.theme.*
import com.agriconnect.data.model.Product

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale

@Composable
fun RecentProductItem(product: Product, onClick: (() -> Unit)? = null) {
    AgriCard(onClick = onClick, modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AgriSecondary),
                contentAlignment = Alignment.Center
            ) {
                if (product.images != null && (product.images!!.startsWith("http") || product.images!!.startsWith("content"))) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.images)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
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
                        tint = AgriPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgriText(
                    text = product.name ?: "Unknown Crop",
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray900,
                    fontWeight = FontWeight.Black
                )
                AgriText(
                    text = "${product.quantity ?: 0f} ${product.unit ?: "units"} • ₹${product.expectedPrice?.toInt() ?: 0}/${product.unit ?: "unit"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    fontWeight = FontWeight.Bold
                )
            }
            val status = product.status ?: "active"
            val statusColor = if (status.lowercase() == "active") Success else Warning
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                AgriText(
                    text = status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun RecentInquiryItem(title: String, buyer: String, status: String = "Pending", onCallClick: () -> Unit = {}) {
    AgriCard(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Info.copy(alpha = 0.1f)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Info, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgriText(title, style = MaterialTheme.typography.titleMedium, color = Gray900, fontWeight = FontWeight.Black)
                AgriText("BUYER: $buyer", style = MaterialTheme.typography.labelSmall, color = Gray500, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onCallClick,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AgriSecondary, contentColor = AgriPrimary)
            ) {
                Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                AgriText("CALL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Gray50),
                contentAlignment = Alignment.Center
            ) {
                if (product.images != null && (product.images!!.startsWith("http") || product.images!!.startsWith("content"))) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.images)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
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
                        tint = AgriPrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(80.dp)
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
                ) {
                    AgriText(
                        text = "VISUAL NODE VERIFIED",
                        style = MaterialTheme.typography.labelSmall,
                        color = White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = AgriSecondary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = AgriPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        AgriText(
                            text = "FARMER-${product.farmerIdAlias ?: product.farmerId?.takeLast(4) ?: "NODE"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray900,
                            fontWeight = FontWeight.Black
                        )
                        AgriText(
                            text = "${product.village ?: "Remote"}, ${product.district ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val status = product.status ?: "active"
                    val statusColor = if (status.lowercase() == "active") Success else Warning
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        AgriText(
                            text = status.uppercase(),
                            color = statusColor,
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
                        AgriText("CROP", style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Black)
                        AgriText(product.name ?: "Unknown", style = MaterialTheme.typography.titleLarge, color = Gray900, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        AgriText("EXPECTED PRICE", style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Black)
                        AgriText("₹${product.expectedPrice?.toInt() ?: "N/A"}", style = MaterialTheme.typography.titleLarge, color = AgriPrimary, fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AgriButton(
                    text = "View Market Details",
                    onClick = onClick,
                    containerColor = Gray900,
                    contentColor = White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun MerchantProductCard(product: Product, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Gray50),
                contentAlignment = Alignment.Center
            ) {
                if (product.images != null && (product.images!!.startsWith("http") || product.images!!.startsWith("content"))) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.images)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
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
                        tint = Gray200,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                // Price Badge
                Surface(
                    color = White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    AgriText(
                        text = "₹${product.expectedPrice?.toInt() ?: "N/A"}/${product.unit ?: "kg"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = AgriPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                AgriText(
                    text = product.name ?: "Unknown",
                    style = MaterialTheme.typography.titleSmall,
                    color = Gray900,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AgriText(
                        text = "${product.quantity ?: 0f} ${product.unit ?: "units"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Grade hint if found in description
                    val grade = remember(product.description) {
                        if (product.description?.contains("A Grade", ignoreCase = true) == true) "A Grade"
                        else if (product.description?.contains("B Grade", ignoreCase = true) == true) "B Grade"
                        else null
                    }
                    
                    if (grade != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        AgriText("•", color = Gray300)
                        Spacer(modifier = Modifier.width(4.dp))
                        AgriText(
                            text = grade as String,
                            style = MaterialTheme.typography.labelSmall,
                            color = Warning,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Gray400, modifier = Modifier.size(10.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    AgriText(
                        text = "${product.village ?: product.district ?: "Verified"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray400,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AgriPrimary,
                        contentColor = White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AgriText(
                            "VIEW NODE", 
                            style = MaterialTheme.typography.labelLarge, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ChevronRight, 
                            null, 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
