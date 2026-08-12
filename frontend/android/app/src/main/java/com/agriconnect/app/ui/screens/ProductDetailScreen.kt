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
    onLoginRequired: () -> Unit
) {
    val token by authViewModel.token
    val user by authViewModel.user
    val products by productViewModel.products
    val currentProduct by productViewModel.currentProduct
    val loading by productViewModel.loading
    val merchantLoading by merchantViewModel.loading

    val product = products.find { it.id == productId } ?: currentProduct
    
    var isBooked by remember { mutableStateOf(false) }
    
    val savedProducts by merchantViewModel.savedProducts
    val isSaved = savedProducts.contains(productId)

    LaunchedEffect(productId) {
        if (products.find { it.id == productId } == null) {
            productViewModel.fetchProductDetail(productId)
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Product Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        if (token == null || user == null) onLoginRequired() 
                        else merchantViewModel.toggleSaveProduct(token!!, user!!.id, productId)
                    }) {
                        Icon(
                            if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) MaterialTheme.colorScheme.error else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (loading && product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (product == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Product not found or unavailable.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (product.images != null && product.images!!.startsWith("http")) {
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
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(120.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "FARMER-${product.farmerIdAlias ?: product.farmerId?.takeLast(4) ?: "NODE"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "${product.district ?: "Verified"} Source", 
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = Color(0xFFECFDF5), 
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "VERIFIED", 
                                color = Color(0xFF059669), 
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = product.name ?: "Unknown Crop", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = product.category?.uppercase() ?: "GENERAL", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = product.description ?: "High-quality agricultural supply direct from the source. Mediated and verified by AgriConnect.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AgriSectionTitle(title = "Supply Parameters")
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SpecBox(Modifier.weight(1f), Icons.Default.Inventory, "Quantity", "${product.quantity ?: 0f} ${product.unit ?: "units"}")
                        SpecBox(Modifier.weight(1f), Icons.Default.LocationOn, "Origin", product.village ?: "Remote")
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Booking Card
                    AgriCard {
                        Text(
                            text = "ESTIMATED MARKET RATE", 
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray, 
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${product.expectedPrice ?: "N/A"}", 
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "/${product.unit ?: "unit"}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray, 
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        if (!isBooked) {
                            AgriButton(
                                text = "Secure this supply",
                                loading = merchantLoading,
                                onClick = { 
                                    if (token == null || user == null) onLoginRequired() 
                                    else {
                                        merchantViewModel.createBooking(token!!, user!!.id, productId) { success ->
                                            if (success) isBooked = true
                                        }
                                    }
                                }
                            )
                        } else {
                            Column(modifier = Modifier.animateContentSize()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("RESERVATION ACTIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                                            Text("Contact Unlocked", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun SpecBox(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
        }
    }
}
