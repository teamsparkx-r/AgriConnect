package com.agriconnect.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailsScreen(onBack: () -> Unit) {
    var accountHolder by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Payment Details",
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
            AgriSectionTitle(title = "Payout Registry", subtitle = "FINANCIAL NODE")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AgriText(
                text = "Secure your payments by linking your bank account. Funds from confirmed deals will be settled directly.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            AgriCard {
                AgriTextField(
                    value = accountHolder,
                    onValueChange = { accountHolder = it },
                    label = "Account Holder Name",
                    leadingIcon = Icons.Outlined.CreditCard
                )
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = "Account Number",
                    leadingIcon = Icons.Outlined.AccountBalance
                )
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(
                    value = ifscCode,
                    onValueChange = { ifscCode = it },
                    label = "IFSC Code"
                )
                Spacer(modifier = Modifier.height(16.dp))
                AgriTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = "Bank Name"
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                AgriButton(
                    text = "SAVE FINANCIAL NODE",
                    onClick = { /* Save logic */ }
                )
            }
            
            AgriFooter()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
