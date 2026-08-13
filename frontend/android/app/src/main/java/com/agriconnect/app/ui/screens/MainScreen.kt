package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.agriconnect.app.ui.navigation.Screen
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.AuthViewModel

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    content: @Composable (PaddingValues) -> Unit
) {
    val user by authViewModel.user
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute !in listOf(
        Screen.Splash.route,
        Screen.RoleSelection.route,
        Screen.Login.route,
        Screen.Signup.route,
        Screen.AdminLogin.route,
        "verify_otp/{mobile}/{role}",
        Screen.AddProduct.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF9FAFB),
        bottomBar = {
            if (showBottomNav) {
                AppBottomNav(
                    role = user?.role ?: "guest",
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = {
                        navController.navigate(Screen.AddProduct.route)
                    }
                )
            }
        }
    ) { padding ->
        content(padding)
    }
}

@Composable
fun AppBottomNav(
    role: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = Emerald600,
        tonalElevation = 0.dp,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(84.dp),
            windowInsets = WindowInsets(0)
        ) {
            when (role) {
                "farmer" -> {
                    NavButton(
                        active = currentRoute == Screen.FarmerDashboard.route,
                        icon = if (currentRoute == Screen.FarmerDashboard.route) Icons.Filled.GridView else Icons.Outlined.GridView,
                        label = "Home",
                        onClick = { onNavigate(Screen.FarmerDashboard.route) }
                    )
                    NavButton(
                        active = currentRoute == Screen.MyProducts.route,
                        icon = if (currentRoute == Screen.MyProducts.route) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                        label = "Stock",
                        onClick = { onNavigate(Screen.MyProducts.route) }
                    )
                    
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        FloatingActionButton(
                            onClick = onAddClick,
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            shape = RoundedCornerShape(16.dp),
                            elevation = FloatingActionButtonDefaults.elevation(4.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Outlined.Add, "Add", modifier = Modifier.size(24.dp))
                        }
                    }

                    NavButton(
                        active = currentRoute == Screen.FarmerBookings.route,
                        icon = if (currentRoute == Screen.FarmerBookings.route) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                        label = "Orders",
                        onClick = { onNavigate(Screen.FarmerBookings.route) }
                    )
                    NavButton(
                        active = currentRoute == Screen.FarmerProfile.route,
                        icon = if (currentRoute == Screen.FarmerProfile.route) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                        label = "Profile",
                        onClick = { onNavigate(Screen.FarmerProfile.route) }
                    )
                }
                else -> { // Merchant / Default
                    NavButton(
                        active = currentRoute == Screen.MerchantPortal.route,
                        icon = if (currentRoute == Screen.MerchantPortal.route) Icons.Filled.Home else Icons.Outlined.Home,
                        label = "Home",
                        onClick = { onNavigate(Screen.MerchantPortal.route) }
                    )
                    NavButton(
                        active = currentRoute == Screen.Explore.route,
                        icon = if (currentRoute == Screen.Explore.route) Icons.Filled.Search else Icons.Outlined.Search,
                        label = "Browse",
                        onClick = { onNavigate(Screen.Explore.route) }
                    )
                    NavButton(
                        active = currentRoute == Screen.Saved.route,
                        icon = if (currentRoute == Screen.Saved.route) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        label = "Saved",
                        onClick = { onNavigate(Screen.Saved.route) }
                    )
                    NavButton(
                        active = currentRoute == Screen.MyBookings.route,
                        icon = if (currentRoute == Screen.MyBookings.route) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                        label = "Orders",
                        onClick = { onNavigate(Screen.MyBookings.route) }
                    )
                    NavButton(
                        active = currentRoute == Screen.MerchantProfile.route,
                        icon = if (currentRoute == Screen.MerchantProfile.route) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                        label = "Profile",
                        onClick = { onNavigate(Screen.MerchantProfile.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.NavButton(
    active: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = active,
        onClick = onClick,
        icon = { 
            Icon(
                imageVector = icon, 
                contentDescription = label, 
                modifier = Modifier.size(24.dp)
            ) 
        },
        label = { 
            Text(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (active) FontWeight.Black else FontWeight.Bold
            ) 
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = Color.Black,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor = Color.Transparent
        )
    )
}
