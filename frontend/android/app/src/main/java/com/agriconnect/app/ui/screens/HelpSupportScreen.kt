package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Help & Support",
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
            // Support Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AgriPrimary,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AgriText("How can we help you?", style = MaterialTheme.typography.titleLarge, color = White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        AgriText("We're here to support you.", style = MaterialTheme.typography.bodyMedium, color = White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.HeadsetMic, null, tint = White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
            ) {
                Column {
                    ProfileListItem(icon = Icons.Outlined.QuestionAnswer, label = "FAQs", subtitle = "Find answers to common questions", onClick = { 
                        android.widget.Toast.makeText(context, "Redirecting to FAQ portal...", android.widget.Toast.LENGTH_SHORT).show()
                    })
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                    ProfileListItem(icon = Icons.Outlined.Chat, label = "Contact Support", subtitle = "Chat or call our support team", onClick = { 
                        android.widget.Toast.makeText(context, "Opening support chat...", android.widget.Toast.LENGTH_SHORT).show()
                    })
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                    ProfileListItem(icon = Icons.Outlined.ReportProblem, label = "Report an Issue", subtitle = "Report a bug or issue", onClick = { 
                        android.widget.Toast.makeText(context, "Loading report form...", android.widget.Toast.LENGTH_SHORT).show()
                    })
                    Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                    ProfileListItem(icon = Icons.Outlined.MenuBook, label = "App Guide", subtitle = "Learn how to use AgriConnect", onClick = { 
                        android.widget.Toast.makeText(context, "Opening app documentation...", android.widget.Toast.LENGTH_SHORT).show()
                    })
                }
            }
            
            AgriFooter()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
