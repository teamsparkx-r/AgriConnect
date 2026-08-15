package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.agriconnect.app.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserListScreen(
    role: String,
    token: String,
    onUserClick: (String) -> Unit,
    onMenuClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val users by viewModel.users
    val loading by viewModel.loading

    val backendRole = remember(role) {
        if (role == "merchant") "buyer" else "farmer"
    }

    LaunchedEffect(backendRole) {
        viewModel.fetchUsers(token, backendRole)
    }

    Scaffold(
        topBar = {
            AgriTopAppBar(
                title = if (role == "farmer") "Farmers" else "Merchants",
                onMenuClick = onMenuClick,
                onNotificationsClick = {},
                onProfileClick = {}
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = White,
                shadowElevation = 0.5.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AgriTextField(
                        value = "",
                        onValueChange = {},
                        label = "Search Registry",
                        placeholder = "Search ${if (role == "farmer") "farmers" else "merchants"}...",
                        leadingIcon = Icons.Default.Search,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = Gray100,
                        shape = RoundedCornerShape(12.dp),
                        onClick = {}
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.padding(12.dp), tint = Gray700)
                    }
                }
            }

            if (loading && users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { userMap ->
                        val id = (userMap["id"] as? String) ?: ""
                        val name = (userMap["full_name"] as? String) ?: "Unknown"
                        val mobile = (userMap["mobile"] as? String) ?: ""
                        val status = (userMap["status"] as? String) ?: "active"
                        val verified = (userMap["verified"] as? Boolean) ?: false
                        val createdAt = (userMap["created_at"] as? String) ?: ""

                        val statusStr = when(status) {
                            "active" -> "Approved"
                            "pending" -> "Pending"
                            "rejected" -> "Rejected"
                            "suspended" -> "Suspended"
                            else -> status.replaceFirstChar { it.uppercase() }
                        }

                        UserListItem(
                            UserStub(
                                id = id,
                                name = name,
                                location = mobile,
                                status = statusStr,
                                statValue = role.replaceFirstChar { it.uppercase() },
                                date = createdAt.take(10)
                            ), 
                            role, 
                            onClick = { onUserClick(id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: UserStub, role: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        shadowElevation = 0.5.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Profile Pic (Avatar)
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = Gray100) {
                Box(contentAlignment = Alignment.Center) {
                    Text(user.name.first().toString(), fontWeight = FontWeight.Black, color = Gray700)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(user.location, color = Gray500, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(user.statValue, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Gray900)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.status, 
                        color = when(user.status) {
                            "Approved" -> AgriPrimary
                            "Pending" -> Warning
                            else -> Error
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(user.date, color = Gray400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class UserStub(
    val id: String,
    val name: String,
    val location: String,
    val status: String,
    val statValue: String,
    val date: String
)
