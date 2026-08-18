package com.agriconnect.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.app.ui.viewmodel.MerchantViewModel
import com.agriconnect.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    merchantViewModel: MerchantViewModel,
    onBack: () -> Unit,
    onLoginRequired: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val token by authViewModel.token
    val user by authViewModel.user
    val products by productViewModel.products
    val currentProduct by productViewModel.currentProduct
    val loading by productViewModel.loading
    val merchantLoading by merchantViewModel.loading

    val product = products.find { it.id == productId } ?: currentProduct
    
    var isBooked by remember { mutableStateOf(false) }
    var showEnquiryDialog by remember { mutableStateOf(false) }
    
    var requestedQty by remember { mutableStateOf(product?.quantity?.toString() ?: "") }
    var proposedPrice by remember { mutableStateOf(product?.expectedPrice?.toString() ?: "") }
    var message by remember { mutableStateOf("") }
    
    val savedProducts by merchantViewModel.savedProducts
    val isSaved = savedProducts.contains(productId)

    LaunchedEffect(productId) {
        if (products.find { it.id == productId } == null) {
            productViewModel.fetchProductDetail(productId)
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Supply Node",
                showLogo = false,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onBackClick = onBack
            )
        }
    ) { padding ->
        if (loading && product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriPrimary)
            }
        } else if (product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                AgriText("Product not found or unavailable.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Image Section with Save Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Gray50)
                ) {
                    if (product.images != null && (product.images!!.startsWith("http") || product.images!!.startsWith("content"))) {
                        AsyncImage(
                            model = product.images,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when(product.category?.lowercase()) {
                                    "vegetables" -> Icons.Default.Agriculture
                                    "fruits" -> Icons.Default.Eco
                                    "grains" -> Icons.Default.Grass
                                    else -> Icons.Default.Inventory
                                },
                                contentDescription = null,
                                tint = AgriPrimary.copy(alpha = 0.1f),
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }
                    
                    // Floating Save Button
                    Surface(
                        onClick = { 
                            if (token == null || user == null) onLoginRequired() 
                            else merchantViewModel.toggleSaveProduct(token!!, user!!.id, productId)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(48.dp),
                        shape = CircleShape,
                        color = White.copy(alpha = 0.9f),
                        shadowElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Save",
                                tint = if (isSaved) Error else Gray400,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = AgriSecondary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AgriPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            AgriText(
                                text = "FARMER-${product.farmerIdAlias ?: product.farmerId?.takeLast(4) ?: "NODE"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Gray900
                            )
                            AgriText(
                                text = "${product.district ?: "Verified"} Source", 
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray400
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = Success.copy(alpha = 0.1f), 
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AgriText(
                                text = "VERIFIED", 
                                color = Success, 
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AgriText(
                        text = product.name ?: "Unknown Crop", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Gray900
                    )
                    AgriText(
                        text = product.category?.uppercase() ?: "GENERAL", 
                        style = MaterialTheme.typography.labelLarge,
                        color = AgriPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AgriText(
                        text = product.description ?: "High-quality agricultural supply direct from the source. Mediated and verified by AgriConnect.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray700,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AgriSectionTitle(title = "Supply Parameters", subtitle = "NODE DATA")
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SpecBox(Modifier.weight(1f), Icons.Default.Inventory, "Quantity", "${product.quantity ?: 0f} ${product.unit ?: "units"}")
                        SpecBox(Modifier.weight(1f), Icons.Default.LocationOn, "Origin", product.village ?: "Remote")
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Booking Card
                    AgriCard {
                        AgriText(
                            text = "ESTIMATED MARKET RATE", 
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray400, 
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            AgriText(
                                text = "₹${product.expectedPrice ?: "N/A"}", 
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Gray900
                            )
                            AgriText(
                                text = "/${product.unit ?: "unit"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Gray400, 
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        if (!isBooked) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            AgriButton(
                                text = "Send Purchase Enquiry",
                                loading = merchantLoading,
                                onClick = { 
                                    if (token == null || user == null) onLoginRequired() 
                                    else if (user?.accountStatus == "pending") {
                                        android.widget.Toast.makeText(context, "Account verification pending. Enquiries restricted.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        showEnquiryDialog = true
                                    }
                                }
                            )
                        } else {
                            Column(modifier = Modifier.animateContentSize()) {
                                Surface(
                                    color = Warning.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Warning.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, null, tint = Warning, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            AgriText("ENQUIRY SENT", color = Warning, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                            AgriText("Awaiting farmer response", color = Warning.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    AgriFooter()
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showEnquiryDialog && product != null) {
        AlertDialog(
            onDismissRequest = { showEnquiryDialog = false },
            title = { AgriText("Send Purchase Enquiry", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AgriText("Negotiate quantity and price directly with the farmer.", style = MaterialTheme.typography.bodySmall, color = Gray600)
                    
                    AgriTextField(
                        value = requestedQty,
                        onValueChange = { requestedQty = it },
                        label = "Required Quantity (${product.unit})",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    
                    AgriTextField(
                        value = proposedPrice,
                        onValueChange = { proposedPrice = it },
                        label = "Expected Price (₹ / ${product.unit})",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    
                    AgriTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = "Message (Optional)",
                        placeholder = "e.g. Need delivery by next week"
                    )
                }
            },
            confirmButton = {
                val context = androidx.compose.ui.platform.LocalContext.current
                AgriButton(
                    text = "CONFIRM ENQUIRY",
                    onClick = {
                        val qty = requestedQty.toFloatOrNull() ?: 0f
                        val price = proposedPrice.toFloatOrNull() ?: 0f
                        
                        if (qty <= 0 || price <= 0) {
                            android.widget.Toast.makeText(context, "Please enter valid quantity and price", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (qty > (product.quantity ?: 0f)) {
                            android.widget.Toast.makeText(context, "Requested quantity exceeds available stock (${product.quantity} ${product.unit})", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            merchantViewModel.createBooking(
                                token = token ?: "",
                                userId = user?.id ?: "",
                                productId = productId,
                                quantity = qty,
                                price = price,
                                message = message
                            ) { success ->
                                if (success) {
                                    isBooked = true
                                    showEnquiryDialog = false
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to send enquiry. Try again.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.width(160.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showEnquiryDialog = false }) {
                    AgriText("CANCEL", color = Gray50, fontWeight = FontWeight.Black)
                }
            },
            containerColor = White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SpecBox(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = AgriPrimary, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            AgriText(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                color = Gray400,
                fontWeight = FontWeight.Bold
            )
            AgriText(
                text = value, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = Gray900
            )
        }
    }
}
