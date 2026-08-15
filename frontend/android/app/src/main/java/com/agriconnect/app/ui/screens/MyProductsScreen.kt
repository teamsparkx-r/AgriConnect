package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.data.model.Product
import androidx.compose.material.icons.outlined.ErrorOutline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    token: String,
    userId: String,
    viewModel: ProductViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onBack: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val products by viewModel.products
    val loading by viewModel.loading
    var activeTab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "Active", "Sold", "Draft")

    LaunchedEffect(token, userId) {
        if (token.isNotEmpty()) {
            viewModel.fetchFarmerProducts(token, userId)
        }
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "My Stock",
                showLogo = false,
                onBackClick = onBack,
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = AgriPrimary,
                contentColor = White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter Tabs
            Surface(
                color = AgriPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = activeTab == tab
                        Surface(
                            onClick = { activeTab = tab },
                            color = if (isSelected) White else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tab,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) AgriPrimary else White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            val error by viewModel.error
            
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriPrimary)
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = Error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = error!!, color = Gray600, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        AgriButton(text = "RETRY", onClick = { viewModel.fetchFarmerProducts(token, userId) })
                    }
                }
            } else if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateCard(message = "No crops yet. Add your first crop to start selling.")
                }
            } else {
                val filteredProducts = remember(activeTab, products) {
                    if (activeTab == "All") products
                    else products.filter { it.status?.lowercase() == activeTab.lowercase() }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProducts) { product ->
                        RecentProductItem(product = product, onClick = { onEditProduct(product.id) })
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
