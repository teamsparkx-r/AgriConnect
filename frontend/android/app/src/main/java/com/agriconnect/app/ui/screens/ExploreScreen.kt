package com.agriconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    token: String,
    viewModel: ProductViewModel,
    onProductClick: (String) -> Unit,
    onBack: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val products by viewModel.products
    val loading by viewModel.loading
    var searchTerm by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    
    // Filter states
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDistrict by remember { mutableStateOf("All") }
    var selectedGrade by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("All") }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var minQty by remember { mutableStateOf("") }
    
    val categories = listOf("All", "Vegetables", "Fruits", "Grains", "Pulses", "Spices", "Oilseeds")
    val districts = remember(products) { 
        listOf("All") + products.mapNotNull { it.district }.distinct().sorted() 
    }
    val grades = listOf("All", "A Grade", "B Grade")
    val statuses = listOf("All", "Active", "Sold")

    LaunchedEffect(token) {
        if (token.isNotEmpty()) {
            viewModel.fetchDiscoveryProducts(token)
        }
    }

    val filteredProducts = remember(searchTerm, products, selectedCategory, selectedDistrict, selectedGrade, selectedStatus, minPrice, maxPrice, minQty) {
        products.filter { product ->
            val matchesSearch = searchTerm.isEmpty() || 
                product.name?.contains(searchTerm, ignoreCase = true) == true || 
                product.district?.contains(searchTerm, ignoreCase = true) == true ||
                product.village?.contains(searchTerm, ignoreCase = true) == true
            
            val matchesCategory = selectedCategory == "All" || 
                product.category?.equals(selectedCategory, ignoreCase = true) == true
            
            val matchesDistrict = selectedDistrict == "All" || 
                product.district?.equals(selectedDistrict, ignoreCase = true) == true

            val matchesGrade = selectedGrade == "All" || 
                product.description?.contains(selectedGrade, ignoreCase = true) == true // Grades are currently in description/logic usually
            
            val matchesStatus = selectedStatus == "All" || 
                product.status?.equals(selectedStatus, ignoreCase = true) == true

            val price = product.expectedPrice ?: 0f
            val matchesMinPrice = minPrice.isEmpty() || price >= (minPrice.toFloatOrNull() ?: 0f)
            val matchesMaxPrice = maxPrice.isEmpty() || price <= (maxPrice.toFloatOrNull() ?: Float.MAX_VALUE)
            
            val qty = product.quantity ?: 0f
            val matchesMinQty = minQty.isEmpty() || qty >= (minQty.toFloatOrNull() ?: 0f)
            
            matchesSearch && matchesCategory && matchesDistrict && matchesGrade && matchesStatus && matchesMinPrice && matchesMaxPrice && matchesMinQty
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text("Marketplace Filters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgriPrimary,
                                selectedLabelColor = White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("District", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(districts) { dist ->
                        FilterChip(
                            selected = selectedDistrict == dist,
                            onClick = { selectedDistrict = dist },
                            label = { Text(dist) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgriPrimary,
                                selectedLabelColor = White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Grade", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    grades.forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgriPrimary,
                                selectedLabelColor = White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Price Range (₹)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AgriTextField(
                        value = minPrice, 
                        onValueChange = { minPrice = it }, 
                        label = "Min Price", 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    AgriTextField(
                        value = maxPrice, 
                        onValueChange = { maxPrice = it }, 
                        label = "Max Price", 
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(
                    value = minQty, 
                    onValueChange = { minQty = it }, 
                    label = "Minimum Quantity Available", 
                    placeholder = "e.g. 100",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AgriButton(
                        text = "CLEAR ALL", 
                        onClick = {
                            selectedCategory = "All"
                            selectedDistrict = "All"
                            selectedGrade = "All"
                            selectedStatus = "All"
                            minPrice = ""
                            maxPrice = ""
                            minQty = ""
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = Gray100,
                        contentColor = Gray900
                    )
                    AgriButton(
                        text = "APPLY FILTERS", 
                        onClick = { showFilters = false },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Global Discovery",
                showLogo = false,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                onMenuClick = onMenuClick
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
                    AgriTextField(
                        value = searchTerm,
                        onValueChange = { searchTerm = it },
                        label = "Marketplace Search",
                        placeholder = "Search crops or locations...",
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = {
                            IconButton(onClick = { showFilters = true }) {
                                Icon(
                                    Icons.Default.Tune, 
                                    contentDescription = "Filter", 
                                    tint = if (selectedCategory != "All" || selectedDistrict != "All" || minPrice.isNotEmpty() || maxPrice.isNotEmpty()) AgriPrimary else Gray400
                                )
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            null, 
                            modifier = Modifier.size(14.dp), 
                            tint = Info
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Displaying ${filteredProducts.size} synchronized supply nodes".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Gray400,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            val error by viewModel.error
            if (loading && products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriPrimary)
                }
            } else if (error != null && products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, null, tint = Gray300, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = error!!, color = Gray600, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    EmptyStateCard(message = "No results match your criteria.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { product ->
                        MerchantProductCard(product, onClick = { onProductClick(product.id) })
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
