package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.components.AgriButton
import com.agriconnect.app.ui.components.AgriTextField
import com.agriconnect.app.ui.viewmodel.AuthViewModel

@Composable
fun VerifyOtpScreen(
    role: String,
    mobile: String,
    viewModel: AuthViewModel,
    onVerifySuccess: (String) -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    val error by viewModel.error
    val loading by viewModel.loading
    
    var showNoAccount by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = "Verify Security Key",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter the 6-digit code sent to +91 $mobile",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        AgriTextField(
            value = otpCode,
            onValueChange = { 
                if (it.length <= 6) {
                    otpCode = it
                    showNoAccount = false
                }
            },
            label = "OTP Code",
            placeholder = "000000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = error != null,
            errorMessage = error
        )
        
        if (showNoAccount) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No account found in our registry.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { onVerifySuccess("none") }) {
                        Text(
                            text = "START REGISTRATION",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!showNoAccount) {
            AgriButton(
                text = "Verify",
                onClick = { 
                    viewModel.verifyOtp(mobile, otpCode) { userRole ->
                        if (userRole == "none") {
                            showNoAccount = true
                        } else {
                            onVerifySuccess(userRole)
                        }
                    }
                },
                loading = loading,
                enabled = otpCode.length == 6
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(
            onClick = { /* Resend OTP */ }
        ) {
            Text(
                text = "RESEND SECURITY CODE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
