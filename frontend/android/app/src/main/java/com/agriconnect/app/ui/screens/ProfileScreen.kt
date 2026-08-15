package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    role: String, 
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onMyFarmClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {}
) {
    val user by authViewModel.user
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AgriTopAppBar(
                title = "Profile",
                showLogo = false,
                onBackClick = onBack
            )
        },
        containerColor = AgriBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Profile Header Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                shadowElevation = 0.5.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = AgriSecondary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user?.fullName?.firstOrNull()?.toString()?.uppercase() ?: "D",
                                style = MaterialTheme.typography.displaySmall,
                                color = AgriPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName ?: (if (role == "admin") "Administrator" else "Demo User"), 
                            style = MaterialTheme.typography.titleLarge, 
                            color = Gray900,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Success, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when(role) {
                                    "admin" -> "Verified Admin"
                                    "farmer" -> "Verified Farmer"
                                    else -> "Verified Merchant"
                                }, 
                                style = MaterialTheme.typography.labelSmall, 
                                color = Gray500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = user?.mobile ?: "+91 98765 43210", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = Gray400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Group
            Text(
                text = "ACCOUNT",
                style = MaterialTheme.typography.labelSmall,
                color = Gray400,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
            ) {
                Column {
                    if (role == "admin") {
                        ProfileListItem(icon = Icons.Outlined.Dashboard, label = "Admin Console", onClick = { })
                        Divider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Gray100)
                        ProfileListItem(icon = Icons.Outlined.Security, label = "Security Audit", onClick = { })
                    } else if (role == "farmer") {
                        ProfileListItem(icon = Icons.Outlined.Agriculture, label = "My Farm", onClick = onMyFarmClick)
                        Divider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Gray100)
                        ProfileListItem(icon = Icons.Outlined.AccountBalance, label = "Bank Details", onClick = { })
                    } else {
                        ProfileListItem(icon = Icons.Outlined.ShoppingBag, label = "My Orders", onClick = { })
                    }
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Gray100)
                    ProfileListItem(icon = Icons.Outlined.Settings, label = "Settings", onClick = onSettingsClick)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Support Group
            Text(
                text = "SUPPORT",
                style = MaterialTheme.typography.labelSmall,
                color = Gray400,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
            ) {
                Column {
                    ProfileListItem(icon = Icons.Outlined.HelpOutline, label = "Help & Support", onClick = onHelpClick)
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), thickness = 0.5.dp, color = Gray100)
                    ProfileListItem(icon = Icons.Outlined.Info, label = "About AgriConnect", onClick = { })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout
            AgriButton(
                text = "Logout",
                onClick = onLogout,
                containerColor = Error.copy(alpha = 0.1f),
                contentColor = Error,
                shape = RoundedCornerShape(18.dp)
            )
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
