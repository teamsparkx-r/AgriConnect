package com.agriconnect.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    object FarmerProductDetail : Screen("farmer/product-detail/{id}") {
        fun createRoute(id: String) = "farmer/product-detail/$id"
    }
    object AddProduct : Screen("farmer/add-product")
    object EditProduct : Screen("farmer/edit-product/{id}") {
        fun createRoute(id: String) = "farmer/edit-product/$id"
    }
    object FarmerBookings : Screen("farmer/bookings")
    object FarmerBookingDetail : Screen("farmer/booking-detail/{id}") {
        fun createRoute(id: String) = "farmer/booking-detail/$id"
    }
    object FarmerProfile : Screen("farmer/profile")
    object FarmerNotifications : Screen("farmer/notifications")

    // Merchant Flow
    object MerchantPortal : Screen("merchant/portal")
    object Explore : Screen("merchant/explore")
    object MyBookings : Screen("merchant/bookings")
    object MerchantBookingDetail : Screen("merchant/booking-detail/{id}") {
        fun createRoute(id: String) = "merchant/booking-detail/$id"
    }
    object Saved : Screen("merchant/saved")
    object MerchantProfile : Screen("merchant/profile")
    object MerchantNotifications : Screen("merchant/notifications")
    object MerchantSettings : Screen("merchant/settings")
    object MerchantHelp : Screen("merchant/help")
    
    // Farmer Common Screens
    object MyFarm : Screen("farmer/my-farm")
    object FarmerSettings : Screen("farmer/settings")
    object FarmerHelp : Screen("farmer/help")

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
    object AdminUserList : Screen("admin/users/{role}") {
        fun createRoute(role: String) = "admin/users/$role"
    }
    object AdminUserDetail : Screen("admin/user-detail/{id}/{role}") {
        fun createRoute(id: String, role: String) = "admin/user-detail/$id/$role"
    }
    object AdminPendingSlots : Screen("admin/pending-slots")
    object AdminCropManagement : Screen("admin/crop-management")
    object AdminVerifications : Screen("admin/verifications")
    object AdminNotifications : Screen("admin/notifications")
    object AdminSettings : Screen("admin/settings")

    object Legal : Screen("legal")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    padding: PaddingValues,
    onMenuClick: () -> Unit = {}
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
                onLoginSuccess = { mobile ->
                    navController.navigate(Screen.VerifyOtp.createRoute(mobile, "admin"))
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
            val user by authViewModel.user
            FarmerDashboardScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                userName = user?.fullName ?: "Farmer",
                viewModel = farmerViewModel,
                authViewModel = authViewModel,
                userStatus = user?.accountStatus ?: "active",
                onBookSlotClick = { navController.navigate(Screen.AddProduct.route) },
                onMyBookingsClick = { navController.navigate(Screen.FarmerBookings.route) },
                onMyProductsClick = { navController.navigate(Screen.MyProducts.route) },
                onProductClick = { id -> navController.navigate(Screen.FarmerProductDetail.createRoute(id)) },
                onNotificationsClick = { navController.navigate(Screen.FarmerNotifications.route) },
                onProfileClick = { navController.navigate(Screen.FarmerProfile.route) },
                onMenuClick = onMenuClick
            )
        }
        composable(Screen.MyProducts.route) {
            val user = authViewModel.user.value
            MyProductsScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = productViewModel,
                onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onEditProduct = { id: String -> navController.navigate(Screen.FarmerProductDetail.createRoute(id)) },
                onBack = { navController.popBackStack() },
                onMenuClick = onMenuClick
            )
        }
        composable(
            route = Screen.FarmerProductDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val user = authViewModel.user.value
            FarmerProductDetailScreen(
                productId = id,
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = productViewModel,
                onEditClick = { productId -> navController.navigate(Screen.EditProduct.createRoute(productId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddProduct.route) {
            val user = authViewModel.user.value
            AddProductScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = productViewModel,
                authViewModel = authViewModel,
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
                authViewModel = authViewModel,
                onNext = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FarmerBookings.route) {
            FarmerBookingsScreen(
                onBack = { navController.popBackStack() },
                onBookingClick = { id -> navController.navigate(Screen.FarmerBookingDetail.createRoute(id)) },
                authViewModel = authViewModel,
                farmerViewModel = farmerViewModel
            )
        }
        composable(
            route = Screen.FarmerBookingDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val user = authViewModel.user.value
            FarmerBookingDetailScreen(
                bookingId = id,
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = farmerViewModel,
                onBack = { navController.popBackStack() }
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
                },
                onMyFarmClick = { navController.navigate(Screen.MyFarm.route) },
                onSettingsClick = { navController.navigate(Screen.FarmerSettings.route) },
                onHelpClick = { navController.navigate(Screen.FarmerHelp.route) }
            )
        }
        composable(Screen.MyFarm.route) {
            MyFarmScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.FarmerSettings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToLegal = { navController.navigate(Screen.Legal.route) }
            )
        }
        composable(Screen.FarmerHelp.route) {
            HelpSupportScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.FarmerNotifications.route) {
            val user = authViewModel.user.value
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                role = "farmer",
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                farmerViewModel = farmerViewModel,
                onNavigateToInquiry = { id -> navController.navigate(Screen.FarmerBookingDetail.createRoute(id)) },
                onNavigateToProfile = { navController.navigate(Screen.FarmerProfile.route) }
            )
        }

        // Merchant Screens
        composable(Screen.MerchantPortal.route) {
            val user by authViewModel.user
            MerchantPortalScreen(
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                productViewModel = productViewModel,
                merchantViewModel = merchantViewModel,
                authViewModel = authViewModel,
                userStatus = user?.accountStatus ?: "active",
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onExploreClick = { navController.navigate(Screen.Explore.route) },
                onMyBookingsClick = { navController.navigate(Screen.MyBookings.route) },
                onNotificationsClick = { navController.navigate(Screen.MerchantNotifications.route) },
                onMenuClick = onMenuClick
            )
        }
        composable(Screen.Explore.route) {
            ExploreScreen(
                token = authViewModel.token.value ?: "",
                viewModel = productViewModel,
                onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                onMenuClick = onMenuClick
            )
        }
        composable(Screen.MyBookings.route) {
            MyBookingsScreen(
                onBack = { navController.popBackStack() },
                onBookingClick = { id -> navController.navigate(Screen.MerchantBookingDetail.createRoute(id)) },
                authViewModel = authViewModel,
                merchantViewModel = merchantViewModel,
                onMenuClick = onMenuClick
            )
        }
        composable(
            route = Screen.MerchantBookingDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val user = authViewModel.user.value
            MerchantBookingDetailScreen(
                bookingId = id,
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                viewModel = merchantViewModel,
                onBack = { navController.popBackStack() }
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
            val user = authViewModel.user.value
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                role = "buyer",
                token = authViewModel.token.value ?: "",
                userId = user?.id ?: "",
                merchantViewModel = merchantViewModel,
                onNavigateToInquiry = { id -> navController.navigate(Screen.MerchantBookingDetail.createRoute(id)) },
                onNavigateToProfile = { navController.navigate(Screen.MerchantProfile.route) }
            )
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
                onPendingSlotsClick = { navController.navigate(Screen.AdminPendingSlots.route) },
                onCropManagementClick = { navController.navigate(Screen.AdminCropManagement.route) },
                onMenuClick = onMenuClick,
                onNotificationsClick = { navController.navigate(Screen.AdminNotifications.route) },
                onProfileClick = { navController.navigate(Screen.AdminProfile.route) }
            )
        }
        composable(Screen.AdminManagement.route) {
            AdminManagementScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminProfile.route) {
            ProfileScreen(
                role = "admin", 
                onBack = { navController.popBackStack() },
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSettingsClick = { navController.navigate(Screen.AdminSettings.route) }
            )
        }
        composable(
            route = Screen.AdminUserList.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "farmer"
            AdminUserListScreen(
                role = role,
                token = authViewModel.token.value ?: "",
                onUserClick = { id -> navController.navigate(Screen.AdminUserDetail.createRoute(id, role)) },
                onMenuClick = onMenuClick,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.AdminUserDetail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: "farmer"
            AdminUserDetailScreen(
                userId = id,
                role = role,
                token = authViewModel.token.value ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminPendingSlots.route) {
            PendingSlotsScreen(
                onMenuClick = onMenuClick,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminCropManagement.route) {
            CropManagementScreen(
                onMenuClick = onMenuClick,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminVerifications.route) {
            LiveVerificationScreen(
                onMenuClick = onMenuClick,
                onBack = { navController.popBackStack() },
                onCallFinished = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminNotifications.route) {
            val adminViewModel: AdminViewModel = viewModel()
            NotificationsScreen(
                onMenuClick = onMenuClick,
                onBack = { navController.popBackStack() },
                role = "admin",
                token = authViewModel.token.value ?: "",
                adminViewModel = adminViewModel,
                onNavigateToApproval = { id, role -> navController.navigate(Screen.AdminUserDetail.createRoute(id, role)) }
            )
        }
        composable(Screen.AdminSettings.route) {
            SettingsScreen(
                onMenuClick = onMenuClick,
                onBack = { navController.popBackStack() },
                onNavigateToLegal = { navController.navigate(Screen.Legal.route) }
            )
        }
    }
}
