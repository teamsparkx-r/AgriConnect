package com.agriconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAuditScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Security Audit",
                showLogo = false,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            AgriSectionTitle(title = "System Integrity", subtitle = "SECURITY")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AgriText(
                text = "Your account is protected by end-to-end encryption. Below is your current security status across the network.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
            ) {
                Column {
                    AuditItem("Encryption Status", "ACTIVE", Success)
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                    AuditItem("Last Sync", "2 minutes ago", Gray600)
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                    AuditItem("Session Identity", "VERIFIED", Success)
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                    AuditItem("2FA Protection", "ENABLED", Success)
                }
            }
            
            AgriFooter()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AuditItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgriText(label, style = MaterialTheme.typography.bodyMedium, color = Gray500, fontWeight = FontWeight.Bold)
        AgriText(value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Black)
    }
}
