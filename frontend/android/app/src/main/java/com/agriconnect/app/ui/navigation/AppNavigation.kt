package com.agriconnect.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.agriconnect.app.ui.screens.*
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.app.ui.viewmodel.FarmerViewModel
import com.agriconnect.app.ui.viewmodel.MerchantViewModel
import com.agriconnect.app.ui.viewmodel.AdminViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object RoleSelection : Screen("role_selection/{mobile}") {
        fun createRoute(mobile: String) = "role_selection/$mobile"
    }
    object Login : Screen("login/{role}") {
        fun createRoute(role: String) = "login/$role"
    }
    object Signup : Screen("signup/{role}/{mobile}") {
        fun createRoute(role: String, mobile: String) = "signup/$role/$mobile"
    }
    object AdminLogin : Screen("admin_login")
    object VerifyOtp : Screen("verify_otp/{mobile}/{role}") {
        fun createRoute(mobile: String, role: String) = "verify_otp/$mobile/$role"
    }

    // Farmer Flow
    object FarmerDashboard : Screen("farmer/dashboard")
    object MyProducts : Screen("farmer/products")
    object AddProduct : Screen("farmer/add-product")
    object EditProduct : Screen("farmer/edit-product/{id}") {
        fun createRoute(id: String) = "farmer/edit-product/$id"
    }
    object FarmerBookings : Screen("farmer/bookings")
    object FarmerProfile : Screen("farmer/profile")
    object FarmerNotifications : Screen("farmer/notifications")

    // Merchant Flow
    object MerchantPortal : Screen("merchant/portal")
    object Explore : Screen("merchant/explore")
    object MyBookings : Screen("merchant/bookings")
    object Saved : Screen("merchant/saved")
    object MerchantProfile : Screen("merchant/profile")
    object MerchantNotifications : Screen("merchant/notifications")
    object ProductDetail : Screen("merchant/product/{id}") {
        fun createRoute(id: String) = "merchant/product/$id"
    }
    object FarmerPublicProfile : Screen("merchant/farmer/{id}") {
        fun createRoute(id: String) = "merchant/farmer/$id"
    }
    object FarmersRegistry : Screen("merchant/farmers")

    // Admin Flow
    object AdminDashboard : Screen("admin/dashboard")
    object AdminManagement : Screen("admin/manage")
    object AdminProfile : Screen("admin/profile")

    object Legal : Screen("legal")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    padding: PaddingValues
) {
    val productViewModel: ProductViewModel = viewModel()
    val farmerViewModel: FarmerViewModel = viewModel()
    val merchantViewModel: MerchantViewModel = viewModel()

    NavHost(
        navController = navController, 
        startDestination = Screen.Splash.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onSplashFinished = {
                val user = authViewModel.user.value
                if (user != null && authViewModel.token.value != null) {
                    val route = when (user.role) {
                        "farmer" -> Screen.FarmerDashboard.route
                        "admin" -> Screen.AdminDashboard.route
                        else -> Screen.MerchantPortal.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Login.createRoute("user")) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            })
        }
        composable(Screen.RoleSelection.route) { backStackEntry ->
            val mobile = backStackEntry.arguments?.getString("mobile") ?: ""
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    navController.navigate(Screen.Signup.createRoute(role, mobile))
                },
                onAdminClick = {
                    navController.navigate(Screen.AdminLogin.route)
                }
            )
        }
        composable(Screen.Login.route) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "user"
            LoginScreen(
                role = role, 
                viewModel = authViewModel,
                onLoginSuccess = { mobile ->
                    navController.navigate(Screen.VerifyOtp.createRoute(mobile, role))
                },
                onSignupClick = {
                    navController.navigate(Screen.RoleSelection.createRoute("unknown"))
                }
            )
        }
        composable(Screen.VerifyOtp.route) { backStackEntry ->
            val mobile = backStackEntry.arguments?.getString("mobile") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "buyer"
            VerifyOtpScreen(
                role = role,
                mobile = mobile,
                viewModel = authViewModel,
                onVerifySuccess = { userRole ->
                    if (userRole == "none") {
                        navController.navigate(Screen.RoleSelection.createRoute(mobile))
                    } else {
                        val route = when (userRole) {
                            "farmer" -> Screen.FarmerDashboard.route
                            "admin" -> Screen.AdminDashboard.route
                            else -> Screen.MerchantPortal.route
                        }
                        navController.navigate(route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.AdminLogin.route) {
            LoginScreen(
                role = "admin",
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                },
                onSignupClick = { /* No admin signup */ }
            )
        }
        composable(Screen.Signup.route) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "buyer"
            val mobile = backStackEntry.arguments?.getString("mobile") ?: ""
            RegistrationScreen(
                role = role, 
                mobile = mobile, 
                viewModel = authViewModel, 
                onRegistrationSuccess = {
                    val route = if (role == "farmer") Screen.FarmerDashboard.route else Screen.MerchantPortal.route
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Farmer Screens
        composable(Screen.FarmerDashboard.route) {
            val user = authViewModel.user.value
            FarmerDashboardScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                userName = user?.fullName ?: "Farmer",
                viewModel = farmerViewModel,
                onBookSlotClick = { navController.navigate(Screen.AddProduct.route) },
                onMyBookingsClick = { navController.navigate(Screen.FarmerBookings.route) },
                onMyProductsClick = { navController.navigate(Screen.MyProducts.route) },
                onNotificationsClick = { navController.navigate(Screen.FarmerNotifications.route) },
                onProfileClick = { navController.navigate(Screen.FarmerProfile.route) }
            )
        }
        composable(Screen.MyProducts.route) {
            val user = authViewModel.user.value
            MyProductsScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = productViewModel,
                onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onEditProduct = { id: String -> navController.navigate(Screen.EditProduct.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddProduct.route) {
            val user = authViewModel.user.value
            AddProductScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = productViewModel,
                onNext = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val user = authViewModel.user.value
            AddProductScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = productViewModel,
                onNext = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
                // isEdit = true logic could be added to AddProductScreen
            )
        }
        composable(Screen.FarmerBookings.route) {
            FarmerBookingsScreen(
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel,
                farmerViewModel = farmerViewModel
            )
        }
        composable(Screen.FarmerProfile.route) {
            ProfileScreen(
                role = "farmer", 
                onBack = { navController.popBackStack() }, 
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.FarmerNotifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        // Merchant Screens
        composable(Screen.MerchantPortal.route) {
            val user = authViewModel.user.value
            MerchantPortalScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                productViewModel = productViewModel,
                merchantViewModel = merchantViewModel,
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onExploreClick = { navController.navigate(Screen.Explore.route) },
                onMyBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                onNotificationsClick = { navController.navigate(Screen.MerchantNotifications.route) }
            )
        }
        composable(Screen.Explore.route) {
            ExploreScreen(
                token = authViewModel.token.value ?: "",
                viewModel = productViewModel,
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
            )
        }
        composable(Screen.MyBookings.route) {
            MyBookingsScreen(
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel,
                merchantViewModel = merchantViewModel
            )
        }
        composable(Screen.Saved.route) {
            SavedScreen(
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel
            )
        }
        composable(Screen.MerchantProfile.route) {
            ProfileScreen(
                role = "buyer", 
                onBack = { navController.popBackStack() }, 
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MerchantNotifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ProductDetailScreen(
                productId = id,
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                merchantViewModel = merchantViewModel,
                onBack = { navController.popBackStack() },
                onLoginRequired = { navController.navigate(Screen.Login.createRoute("buyer")) }
            )
        }
        composable(
            route = Screen.FarmerPublicProfile.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            FarmerPublicProfileScreen(farmerId = id, onBack = { navController.popBackStack() })
        }
        composable(Screen.FarmersRegistry.route) {
            FarmersRegistryScreen(
                onFarmerClick = { id -> navController.navigate(Screen.FarmerPublicProfile.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Legal.route) {
            LegalScreen(onBack = { navController.popBackStack() })
        }

        // Admin Screens
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                token = authViewModel.token.value ?: "",
                onPendingSlotsClick = { navController.navigate(Screen.AdminManagement.route) },
                onCropManagementClick = { navController.navigate(Screen.AdminManagement.route) }
            )
        }
        composable(Screen.AdminManagement.route) {
            AdminManagementScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminProfile.route) {
            ProfileScreen(role = "admin", onBack = { navController.popBackStack() })
        }
    }
}
