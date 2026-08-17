package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Legal & Protocol",
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
            AgriSectionTitle(title = "Network Protocol", subtitle = "GOVERNANCE")
            Spacer(modifier = Modifier.height(16.dp))
            AgriText(
                text = "AgriConnect operates as a decentralized marketplace. Our protocol ensures 0% mediator fees by enabling direct negotiation between rural producers and commercial buyers.\n\nAll supply nodes are subject to quality audit by authorized network agents. By using the platform, you agree to the verified sourcing standards.",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray700,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AgriSectionTitle(title = "Privacy Policy", subtitle = "IDENTITY")
            Spacer(modifier = Modifier.height(12.dp))
            AgriText(
                text = "To protect the supply chain, identities remain anonymous until a reservation is confirmed. Location data is stored securely in our registry.\n\nWe collect minimal personal data necessary for account verification and marketplace transparency. Your information is never sold to third-party advertisers.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            AgriSectionTitle(title = "Terms of Use", subtitle = "AGREEMENT")
            Spacer(modifier = Modifier.height(12.dp))
            AgriText(
                text = "1. Accurate Information: Users must provide truthful data regarding crop quality and business details.\n\n2. Direct Sourcing: AgriConnect prohibits off-platform circumvention once an enquiry is initiated through our mediation layer.\n\n3. Quality Standards: Farmers are responsible for the physical produce meeting the digital node description.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                lineHeight = 22.sp
            )
            
            AgriFooter()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
