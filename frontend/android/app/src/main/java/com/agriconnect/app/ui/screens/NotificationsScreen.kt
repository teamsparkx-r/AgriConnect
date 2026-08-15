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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onMenuClick: (() -> Unit)? = null,
    onBack: () -> Unit,
    role: String = "farmer",
    token: String = "",
    userId: String = "",
    farmerViewModel: com.agriconnect.app.ui.viewmodel.FarmerViewModel? = null,
    merchantViewModel: com.agriconnect.app.ui.viewmodel.MerchantViewModel? = null,
    adminViewModel: com.agriconnect.app.ui.viewmodel.AdminViewModel? = null,
    onNavigateToInquiry: (String) -> Unit = {},
    onNavigateToApproval: (String, String) -> Unit = { _, _ -> },
    onNavigateToProfile: () -> Unit = {}
) {
    LaunchedEffect(key1 = true) {
        if (token.isNotEmpty()) {
            when (role) {
                "farmer" -> {
                    farmerViewModel?.fetchNotifications(token, userId)
                    farmerViewModel?.markAsRead(token, userId)
                }
                "buyer" -> {
                    merchantViewModel?.fetchNotifications(token, userId)
                    merchantViewModel?.markAsRead(token, userId)
                }
                "admin" -> {
                    adminViewModel?.fetchNotifications(token)
                    adminViewModel?.markAsRead(token)
                }
            }
        }
    }

    val notifications = when (role) {
        "farmer" -> farmerViewModel?.notifications?.value ?: emptyList()
        "buyer" -> merchantViewModel?.notifications?.value ?: emptyList()
        "admin" -> adminViewModel?.notifications?.value ?: emptyList()
        else -> emptyList()
    }

    val unread = when (role) {
        "farmer" -> (farmerViewModel?.dashboardData?.value?.stats?.unreadMessages ?: 0) > 0
        "buyer" -> merchantViewModel?.hasUnread?.value ?: false
        "admin" -> adminViewModel?.hasUnread?.value ?: false
        else -> false
    }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Notifications",
                hasUnreadNotifications = unread,
                onMenuClick = onMenuClick,
                onBackClick = onBack
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp), 
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(message = "Your notification log is clear. We'll alert you here regarding supply matching and fulfillment updates.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(notifications) { item ->
                    val type = item["notification_type"] as? String ?: ""
                    val title = item["title"] as? String ?: "Notification"
                    val message = item["message"] as? String ?: ""
                    val relatedId = item["related_id"] as? String ?: ""
                    val createdAt = item["created_at"] as? String ?: ""
                    
                    NotificationCard(
                        title = title,
                        description = message,
                        time = createdAt,
                        icon = when (type) {
                            "new_enquiry", "counter_offer", "merchant_responded" -> Icons.Default.Handshake
                            "order_confirmed" -> Icons.Default.CheckCircle
                            "account_approved" -> Icons.Default.VerifiedUser
                            "account_rejected" -> Icons.Default.Error
                            "new_farmer_registration", "new_merchant_registration" -> Icons.Default.PersonAdd
                            else -> Icons.Default.Notifications
                        },
                        onClick = {
                            if (relatedId.isNotEmpty()) {
                                when (type) {
                                    "new_enquiry", "counter_offer", "merchant_responded", "order_confirmed", "negotiation_rejected" -> onNavigateToInquiry(relatedId)
                                    "new_farmer_registration" -> onNavigateToApproval(relatedId, "farmer")
                                    "new_merchant_registration" -> onNavigateToApproval(relatedId, "merchant")
                                    "account_approved", "account_rejected" -> onNavigateToProfile()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(title: String, description: String, time: String, icon: ImageVector, onClick: () -> Unit) {
    AgriCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AgriSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = AgriPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Gray900)
                    Text(text = time.take(10), style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Bold)
                }
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Gray600, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
            }
        }
    }
}
