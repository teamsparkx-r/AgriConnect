package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import com.agriconnect.app.ui.viewmodel.MerchantViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onProductClick: (String) -> Unit,
    onBack: () -> Unit = {},
    authViewModel: AuthViewModel,
    merchantViewModel: MerchantViewModel = viewModel()
) {
    val token by authViewModel.token
    val user by authViewModel.user
    val savedProducts by merchantViewModel.savedProductsList
    val loading by merchantViewModel.loading

    LaunchedEffect(token, user) {
        if (token != null && user != null) {
            merchantViewModel.fetchSavedProducts(token!!, user!!.id)
        }
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Saved Nodes",
                showLogo = false,
                onBackClick = onBack
            )
        }
    ) { padding ->
        if (loading && savedProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriPrimary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (savedProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateCard(message = "Your saved supply nodes will appear here. Track interesting crops by marking them as favorites during discovery.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AgriSectionTitle(title = "Your Watchlist", subtitle = "FAVORITES")
                        }
                        items(savedProducts) { product ->
                            MerchantProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                        item {
                            AgriFooter()
                        }
                    }
                }
            }
        }
    }
}
