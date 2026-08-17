package com.agriconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
fun RatingScreen(onBack: () -> Unit) {
    var rating by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Rate AgriConnect",
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AgriSectionTitle(title = "Your Feedback", subtitle = "GROWTH", modifier = Modifier.align(Alignment.Start))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AgriText(
                text = "Help us improve the decentralized agricultural network. Your rating directly impacts our platform's evolution.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                lineHeight = 22.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { index ->
                    IconButton(
                        onClick = { rating = index },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (index <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (index <= rating) Warning else Gray300,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AgriTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = "Share your thoughts",
                placeholder = "What features would you like to see next?",
                modifier = Modifier.height(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AgriButton(
                text = "SUBMIT FEEDBACK",
                onClick = { /* Submit logic */ },
                enabled = rating > 0
            )
            
            AgriFooter()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
