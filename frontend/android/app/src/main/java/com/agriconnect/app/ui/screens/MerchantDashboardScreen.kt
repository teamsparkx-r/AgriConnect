package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(
    token: String,
    viewModel: ProductViewModel,
    onSlotClick: (String) -> Unit
) {
    val products by viewModel.products
    val loading by viewModel.loading
    val error by viewModel.error

    LaunchedEffect(key1 = true) {
        viewModel.fetchDiscoveryProducts(token)
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                onProfileClick = { },
                onNotificationsClick = { }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Discovery Toolbar
            Surface(
                color = White,
                shadowElevation = 0.5.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Market Overview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Hello, Merchant! 👋",
                        style = MaterialTheme.typography.displaySmall,
                        color = Gray900,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Search
                    AgriTextField(
                        value = "",
                        onValueChange = { },
                        label = "Marketplace Search",
                        placeholder = "Search crops or locations...",
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Gray400)
                            }
                        }
                    )
                }
            }

            AgriSectionTitle(
                title = "Available Supply Nodes", 
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products) { product ->
                        MerchantProductCard(product, onClick = { onSlotClick(product.id) })
                    }
                }
            }
        }
    }
}
