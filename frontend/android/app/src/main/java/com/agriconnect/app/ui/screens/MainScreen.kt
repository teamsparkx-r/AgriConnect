package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.agriconnect.app.ui.navigation.Screen
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import com.agriconnect.app.ui.viewmodel.VoiceAgentViewModel
import com.agriconnect.app.ui.viewmodel.AssistantState
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.utils.AudioRecorder
import com.agriconnect.data.model.AIContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.core.content.ContextCompat
import java.io.File

@Composable
fun MainScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    voiceViewModel: VoiceAgentViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = VoiceAgentViewModel.provideFactory(LocalContext.current)
    ),
    content: @Composable (PaddingValues, () -> Unit) -> Unit
) {
    val userState by authViewModel.user
    val user = userState 
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val assistantState by voiceViewModel.state
    val transcribedText by voiceViewModel.transcribedText
    val responseText by voiceViewModel.responseText
    
    val context = LocalContext.current
    val recorder = remember { AudioRecorder(context) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    val handleAIAction: (com.agriconnect.data.model.AIActionResponse) -> Unit = { aiAction ->
        when (aiAction.action) {
            "NAVIGATE" -> {
                aiAction.target?.let { route ->
                    navController.navigate(route)
                }
            }
            "SEARCH", "CHECK_PRICE" -> {
                // Navigate to explore to see listings and prices
                navController.navigate(Screen.Explore.route)
            }
            "ADD_PRODUCT" -> {
                if (user?.role?.lowercase() == "farmer") {
                    navController.navigate(Screen.AddProduct.route)
                }
            }
            "VIEW_OFFERS" -> {
                val route = if (user?.role?.lowercase() == "farmer") Screen.FarmerBookings.route else Screen.MyBookings.route
                navController.navigate(route)
            }
            "EXPLAIN" -> {
                // UI shows the responseText via the overlay automatically
            }
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (assistantState == AssistantState.LISTENING) 1.2f else 1f,
        animationSpec = if (assistantState == AssistantState.LISTENING) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(300)
        },
        label = "pulse"
    )

    // Initialize the AI Voice Engine
    LaunchedEffect(Unit) {
        voiceViewModel.initTTS(context)
    }

    // Auto-stop recording after 12 seconds to prevent "infinite listening"
    LaunchedEffect(assistantState) {
        if (assistantState == AssistantState.LISTENING) {
            kotlinx.coroutines.delay(12000)
            if (voiceViewModel.state.value == AssistantState.LISTENING) {
                recorder.stop()
                val userRole = user?.role?.lowercase() ?: "guest"
                voiceViewModel.stopListening(
                    audioFile = audioFile,
                    context = AIContext(
                        role = userRole,
                        currentScreen = currentRoute ?: "unknown",
                        userId = user?.id
                    ),
                    onAction = handleAIAction
                )
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "voice_input.m4a")
            audioFile = file
            voiceViewModel.startListening()
            recorder.start(file)
        } else {
            android.widget.Toast.makeText(context, "Microphone permission is required for the AI Assistant", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val showBottomNav = currentRoute !in listOf(
        Screen.Splash.route,
        Screen.RoleSelection.route,
        Screen.Login.route,
        Screen.Signup.route,
        Screen.AdminLogin.route,
        "verify_otp/{mobile}/{role}",
        Screen.AddProduct.route
    )

    val onNavigate: (String) -> Unit = { route ->
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (user != null) {
            val userRole = user.role.lowercase().trim()
            
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    when {
                        userRole == "admin" -> AdminDrawerContent(currentRoute, onNavigate, user.fullName)
                        userRole == "farmer" -> FarmerDrawerContent(currentRoute, onNavigate, user.fullName)
                        else -> MerchantDrawerContent(currentRoute, onNavigate, user.fullName)
                    }
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF9FAFB),
                    bottomBar = {
                        if (showBottomNav) {
                            AppBottomNav(
                                role = userRole,
                                currentRoute = currentRoute,
                                onNavigate = onNavigate,
                                onAddClick = {
                                    if (userRole == "farmer") {
                                        navController.navigate(Screen.AddProduct.route)
                                    } else {
                                        scope.launch { drawerState.open() }
                                    }
                                }
                            )
                        }
                    },
                    floatingActionButton = {
                        if (showBottomNav) {
                            FloatingActionButton(
                                onClick = { 
                                    if (assistantState == AssistantState.IDLE) {
                                        // Request permission and start
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            val file = File(context.cacheDir, "voice_input.m4a")
                                            audioFile = file
                                            voiceViewModel.startListening()
                                            recorder.start(file)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    } else if (assistantState == AssistantState.LISTENING) {
                                        // Stop recording
                                        recorder.stop()
                                        voiceViewModel.stopListening(
                                            audioFile = audioFile,
                                            context = AIContext(
                                                role = userRole,
                                                currentScreen = currentRoute ?: "unknown",
                                                userId = user.id
                                            ),
                                            onAction = handleAIAction
                                        )
                                    }
                                },
                                containerColor = if (assistantState == AssistantState.LISTENING) Color.Red else Color.Black,
                                contentColor = White,
                                shape = CircleShape,
                                modifier = Modifier.size(64.dp).scale(pulseScale)
                            ) {
                                Icon(
                                    imageVector = if (assistantState == AssistantState.LISTENING) Icons.Filled.Stop else Icons.Default.Mic,
                                    contentDescription = "Assistant", 
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                ) { padding ->
                    content(padding) { scope.launch { drawerState.open() } }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color(0xFFF9FAFB),
                bottomBar = {
                    if (showBottomNav) {
                        AppBottomNav(
                            role = "guest",
                            currentRoute = currentRoute,
                            onNavigate = onNavigate,
                            onAddClick = { }
                        )
                    }
                }
            ) { padding ->
                content(padding) { }
            }
        }

        // Voice Overlay
        if (user != null) {
            val userRole = user.role.lowercase().trim()
            VoiceAssistantOverlay(
                state = assistantState,
                transcribedText = transcribedText,
                responseText = responseText,
                requiresConfirmation = voiceViewModel.lastAction.value?.requiresConfirmation ?: false,
                onConfirm = { voiceViewModel.confirmAction(handleAIAction) },
                onDismiss = { 
                    recorder.stop()
                    voiceViewModel.dismiss() 
                },
                onStop = {
                    recorder.stop()
                    voiceViewModel.stopListening(
                        audioFile = audioFile,
                        context = AIContext(
                            role = userRole,
                            currentScreen = currentRoute ?: "unknown",
                            userId = user.id
                        ),
                        onAction = handleAIAction
                    )
                }
            )
        }
    }
}

@Composable
fun AdminDrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    userName: String
) {
    DrawerTemplate(
        roleLabel = "Admin Panel",
        userName = userName,
        currentRoute = currentRoute,
        onNavigate = onNavigate
    ) {
        DrawerSection("MAIN")
        DrawerItem(
            label = "Dashboard",
            icon = Icons.Default.Dashboard,
            selected = currentRoute == Screen.AdminDashboard.route,
            onClick = { onNavigate(Screen.AdminDashboard.route) }
        )
        DrawerItem(
            label = "Farmers",
            icon = Icons.Default.Agriculture,
            selected = currentRoute?.startsWith("admin/users/farmer") == true,
            onClick = { onNavigate(Screen.AdminUserList.createRoute("farmer")) }
        )
        DrawerItem(
            label = "Merchants",
            icon = Icons.Default.Storefront,
            selected = currentRoute?.startsWith("admin/users/merchant") == true,
            onClick = { onNavigate(Screen.AdminUserList.createRoute("merchant")) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        DrawerSection("MANAGEMENT")
        DrawerItem(
            label = "Pending Slots",
            icon = Icons.Default.Rule,
            selected = currentRoute == Screen.AdminPendingSlots.route,
            onClick = { onNavigate(Screen.AdminPendingSlots.route) }
        )
        DrawerItem(
            label = "Crop Inventory",
            icon = Icons.Default.Inventory2,
            selected = currentRoute == Screen.AdminCropManagement.route,
            onClick = { onNavigate(Screen.AdminCropManagement.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        DrawerSection("SYSTEM")
        DrawerItem(
            label = "Notifications",
            icon = Icons.Default.Notifications,
            selected = currentRoute == Screen.AdminNotifications.route,
            onClick = { onNavigate(Screen.AdminNotifications.route) }
        )
        DrawerItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            selected = currentRoute == Screen.AdminSettings.route,
            onClick = { onNavigate(Screen.AdminSettings.route) }
        )
    }
}

@Composable
fun FarmerDrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    userName: String
) {
    DrawerTemplate(
        roleLabel = "Farmer Portal",
        userName = userName,
        currentRoute = currentRoute,
        onNavigate = onNavigate
    ) {
        DrawerSection("MY FARM")
        DrawerItem(
            label = "Dashboard",
            icon = Icons.Default.GridView,
            selected = currentRoute == Screen.FarmerDashboard.route,
            onClick = { onNavigate(Screen.FarmerDashboard.route) }
        )
        DrawerItem(
            label = "My Products",
            icon = Icons.Default.Inventory2,
            selected = currentRoute == Screen.MyProducts.route,
            onClick = { onNavigate(Screen.MyProducts.route) }
        )
        DrawerItem(
            label = "My Bookings",
            icon = Icons.Default.ReceiptLong,
            selected = currentRoute == Screen.FarmerBookings.route,
            onClick = { onNavigate(Screen.FarmerBookings.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        DrawerSection("ACCOUNT")
        DrawerItem(
            label = "Profile",
            icon = Icons.Default.AccountCircle,
            selected = currentRoute == Screen.FarmerProfile.route,
            onClick = { onNavigate(Screen.FarmerProfile.route) }
        )
        DrawerItem(
            label = "Notifications",
            icon = Icons.Default.Notifications,
            selected = currentRoute == Screen.FarmerNotifications.route,
            onClick = { onNavigate(Screen.FarmerNotifications.route) }
        )
        DrawerItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            selected = currentRoute == Screen.FarmerSettings.route,
            onClick = { onNavigate(Screen.FarmerSettings.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        DrawerSection("SUPPORT")
        DrawerItem(
            label = "Help & Support",
            icon = Icons.Default.HelpOutline,
            selected = currentRoute == Screen.FarmerHelp.route,
            onClick = { onNavigate(Screen.FarmerHelp.route) }
        )
        DrawerItem(
            label = "About",
            icon = Icons.Default.Info,
            selected = currentRoute == Screen.Legal.route,
            onClick = { onNavigate(Screen.Legal.route) }
        )
    }
}

@Composable
fun MerchantDrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    userName: String
) {
    DrawerTemplate(
        roleLabel = "Merchant Portal",
        userName = userName,
        currentRoute = currentRoute,
        onNavigate = onNavigate
    ) {
        DrawerSection("MARKETPLACE")
        DrawerItem(
            label = "Home",
            icon = Icons.Default.Home,
            selected = currentRoute == Screen.MerchantPortal.route,
            onClick = { onNavigate(Screen.MerchantPortal.route) }
        )
        DrawerItem(
            label = "Browse Crops",
            icon = Icons.Default.Search,
            selected = currentRoute == Screen.Explore.route,
            onClick = { onNavigate(Screen.Explore.route) }
        )
        DrawerItem(
            label = "My Orders",
            icon = Icons.Default.ShoppingBag,
            selected = currentRoute == Screen.MyBookings.route,
            onClick = { onNavigate(Screen.MyBookings.route) }
        )
        DrawerItem(
            label = "Saved",
            icon = Icons.Default.Favorite,
            selected = currentRoute == Screen.Saved.route,
            onClick = { onNavigate(Screen.Saved.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        DrawerSection("ACCOUNT")
        DrawerItem(
            label = "Profile",
            icon = Icons.Default.AccountCircle,
            selected = currentRoute == Screen.MerchantProfile.route,
            onClick = { onNavigate(Screen.MerchantProfile.route) }
        )
        DrawerItem(
            label = "Notifications",
            icon = Icons.Default.Notifications,
            selected = currentRoute == Screen.MerchantNotifications.route,
            onClick = { onNavigate(Screen.MerchantNotifications.route) }
        )
        DrawerItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            selected = currentRoute == Screen.MerchantSettings.route,
            onClick = { onNavigate(Screen.MerchantSettings.route) }
        )

        Spacer(modifier = Modifier.height(16.dp))
        DrawerSection("SUPPORT")
        DrawerItem(
            label = "Help & Support",
            icon = Icons.Default.HelpOutline,
            selected = currentRoute == Screen.MerchantHelp.route,
            onClick = { onNavigate(Screen.MerchantHelp.route) }
        )
        DrawerItem(
            label = "About",
            icon = Icons.Default.Info,
            selected = currentRoute == Screen.Legal.route,
            onClick = { onNavigate(Screen.Legal.route) }
        )
    }
}

@Composable
fun DrawerTemplate(
    roleLabel: String,
    userName: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = White,
        drawerTonalElevation = 0.dp,
        drawerShape = RoundedCornerShape(0.dp),
        modifier = Modifier.width(300.dp).fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AgriPrimary)
                    .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Icon(Icons.Default.Eco, null, tint = White, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    AgriText("AgriConnect", color = White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    AgriText(roleLabel, color = White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = White.copy(alpha = 0.2f)) {
                            Box(contentAlignment = Alignment.Center) {
                                AgriText(userName.takeIf { it.isNotEmpty() }?.first()?.toString() ?: "U", color = White, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AgriText(userName, color = White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                content()
                
                Spacer(modifier = Modifier.height(40.dp))
                AgriText(
                    "Version 1.0.4-Build.2023",
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray400
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
fun DrawerSection(label: String) {
    AgriText(
        text = label,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        color = Gray400,
        letterSpacing = 1.sp
    )
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { AgriText(label, fontWeight = if (selected) FontWeight.Black else FontWeight.Bold) },
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null) },
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = AgriPrimary.copy(alpha = 0.1f),
            selectedIconColor = AgriPrimary,
            selectedTextColor = AgriPrimary,
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = Gray500,
            unselectedTextColor = Gray700
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun AppBottomNav(
    role: String,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = AgriPrimary,
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
            when {
                role == "admin" -> {
                    NavButton(
                        active = currentRoute == Screen.AdminDashboard.route,
                        icon = if (currentRoute == Screen.AdminDashboard.route) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                        label = "Console",
                        onClick = { onNavigate(Screen.AdminDashboard.route) }
                    )
                    NavButton(
                        active = currentRoute?.startsWith("admin/users/farmer") == true,
                        icon = if (currentRoute?.startsWith("admin/users/farmer") == true) Icons.Filled.Agriculture else Icons.Outlined.Agriculture,
                        label = "Farmers",
                        onClick = { onNavigate(Screen.AdminUserList.createRoute("farmer")) }
                    )
                    NavButton(
                        active = currentRoute?.startsWith("admin/users/merchant") == true,
                        icon = if (currentRoute?.startsWith("admin/users/merchant") == true) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                        label = "Merchants",
                        onClick = { onNavigate(Screen.AdminUserList.createRoute("merchant")) }
                    )
                    NavButton(
                        active = currentRoute == Screen.AdminSettings.route,
                        icon = if (currentRoute == Screen.AdminSettings.route) Icons.Filled.Settings else Icons.Outlined.Settings,
                        label = "Settings",
                        onClick = { onNavigate(Screen.AdminSettings.route) }
                    )
                }
                role == "farmer" -> {
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
                            containerColor = White,
                            contentColor = Gray900,
                            shape = RoundedCornerShape(18.dp),
                            elevation = FloatingActionButtonDefaults.elevation(6.dp),
                            modifier = Modifier.size(56.dp).offset(y = (-12).dp) // Raised effect
                        ) {
                            Icon(Icons.Filled.Add, "Add", modifier = Modifier.size(28.dp))
                        }
                    }

                    NavButton(
                        active = currentRoute == Screen.FarmerBookings.route,
                        icon = if (currentRoute == Screen.FarmerBookings.route) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                        label = "Orders",
                        onClick = { onNavigate(Screen.FarmerBookings.route) }
                    )
                    NavButton(
                        active = currentRoute == Screen.FarmerSettings.route,
                        icon = if (currentRoute == Screen.FarmerSettings.route) Icons.Filled.Settings else Icons.Outlined.Settings,
                        label = "Settings",
                        onClick = { onNavigate(Screen.FarmerSettings.route) }
                    )
                }
                else -> { // Merchant / Buyer / Default
                    val isMerchant = role == "merchant" || role == "buyer" || role == "guest"
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
                        active = currentRoute == Screen.MerchantSettings.route,
                        icon = if (currentRoute == Screen.MerchantSettings.route) Icons.Filled.Settings else Icons.Outlined.Settings,
                        label = "Settings",
                        onClick = { onNavigate(Screen.MerchantSettings.route) }
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
            AgriText(
                text = label, 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (active) FontWeight.Black else FontWeight.Bold
            ) 
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.Black,
            selectedTextColor = Color.Black,
            unselectedIconColor = Color.White,
            unselectedTextColor = Color.White,
            indicatorColor = Color.White.copy(alpha = 0.9f)
        )
    )
}
