package com.agriconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriTextField
import com.agriconnect.app.ui.components.SlotCard
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    token: String,
    viewModel: ProductViewModel,
    onProductClick: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val products by viewModel.products
    val loading by viewModel.loading
    var searchTerm by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            viewModel.fetchDiscoveryProducts(token)
        }
    }

    val filteredProducts = remember(searchTerm, products) {
        if (searchTerm.isEmpty()) products
        else products.filter { 
            it.name?.contains(searchTerm, ignoreCase = true) == true || 
            it.category?.contains(searchTerm, ignoreCase = true) == true ||
            it.district?.contains(searchTerm, ignoreCase = true) == true ||
            it.village?.contains(searchTerm, ignoreCase = true) == true
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Global Discovery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Emerald600)
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
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    AgriTextField(
                        value = searchTerm,
                        onValueChange = { searchTerm = it },
                        label = "Marketplace Search",
                        placeholder = "Search crops or locations...",
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Color.Gray)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            null, 
                            modifier = Modifier.size(14.dp), 
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Displaying ${filteredProducts.size} synchronized supply nodes".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            val error by viewModel.error
            if (loading && products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (error != null && products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = error!!, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No results match your search.", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        SlotCard(product, onClick = { onProductClick(product.id) })
                    }
                }
            }
        }
    }
}
