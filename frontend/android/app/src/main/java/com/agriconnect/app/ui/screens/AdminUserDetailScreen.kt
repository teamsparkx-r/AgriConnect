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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriTopAppBar
import com.agriconnect.app.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.agriconnect.app.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    userId: String,
    role: String,
    token: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val user by viewModel.userDetail
    val loading by viewModel.loading

    LaunchedEffect(userId) {
        viewModel.fetchUserDetail(token, userId)
    }
    
    val userName = user?.get("full_name") as? String ?: "Loading..."
    val status = user?.get("status") as? String ?: "active"
    val profile = user?.get("profile") as? Map<String, Any>
    val reportCount = 0 // Reports not fully implemented in backend yet as a list in user detail

    Scaffold(
        topBar = {
            AgriTopAppBar(
                title = "User Audit",
                showLogo = false,
                onBackClick = onBack
            )
        },
        containerColor = AgriBackground
    ) { padding ->
        if (loading && user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AgriPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Profile Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(96.dp),
                            shape = CircleShape,
                            color = AgriSecondary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userName.firstOrNull()?.toString() ?: "?",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = AgriPrimary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Gray900
                        )
                        Text(
                            text = "UID: $userId",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray400,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            UserStatItem(label = "Status", value = status.replaceFirstChar { it.uppercase() }, color = if (status == "active") Success else Error)
                            UserStatItem(label = "Role", value = role.replaceFirstChar { it.uppercase() }, color = Info)
                            UserStatItem(label = "Rating", value = profile?.get("rating")?.toString() ?: "N/A", color = Warning)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Supply Parameters Section (For Farmers) or Business Info (For Merchants)
                Text(
                    text = "SUPPLY PARAMETERS",
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
                    Column(modifier = Modifier.padding(20.dp)) {
                        DetailRow("State", profile?.get("state")?.toString() ?: "N/A")
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100, thickness = 0.5.dp)
                        DetailRow("District", profile?.get("district")?.toString() ?: "N/A")
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100, thickness = 0.5.dp)
                        if (role == "farmer") {
                            DetailRow("Village", profile?.get("village")?.toString() ?: "N/A")
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100, thickness = 0.5.dp)
                            DetailRow("Listings", profile?.get("listings_count")?.toString() ?: "0")
                        } else {
                            DetailRow("Merchant Type", profile?.get("type")?.toString() ?: "Retailer")
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Gray100, thickness = 0.5.dp)
                            DetailRow("Bookings", profile?.get("bookings_count")?.toString() ?: "0")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Actions
                if (status == "pending") {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AgriButton(
                            text = "APPROVE",
                            onClick = { viewModel.approveUser(token, userId) { } },
                            containerColor = Success,
                            contentColor = White,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f)
                        )
                        AgriButton(
                            text = "REJECT",
                            onClick = { viewModel.rejectUser(token, userId) { } },
                            containerColor = Error.copy(alpha = 0.1f),
                            contentColor = Error,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    AgriButton(
                        text = if (status == "active") "Suspend Node" else "Activate Node",
                        onClick = { },
                        containerColor = if (status == "active") Error.copy(alpha = 0.1f) else Success.copy(alpha = 0.1f),
                        contentColor = if (status == "active") Error else Success,
                        shape = RoundedCornerShape(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Gray500, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Text(value, color = Gray900, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
    }
}

@Composable
fun UserStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ReportCard(title: String, desc: String, date: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, tint = Error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Gray900)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Gray600)
            Spacer(modifier = Modifier.height(12.dp))
            Text(date, style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AgriButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text, fontWeight = FontWeight.Black)
    }
}
