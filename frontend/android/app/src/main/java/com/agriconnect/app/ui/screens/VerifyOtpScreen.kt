package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.agriconnect.app.ui.components.AgriText
import com.agriconnect.app.ui.theme.AgriPrimary
import com.agriconnect.app.ui.theme.AgriBackground
import com.agriconnect.app.ui.theme.White
import com.agriconnect.app.ui.theme.Gray600
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
            .background(AgriBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Unified Header
        Surface(
            color = AgriPrimary,
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(top = 40.dp, bottom = 48.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AgriText(
                    text = "Verify Security Key",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AgriText(
                    text = "Enter the 6-digit code sent to +91 $mobile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AgriText(
                            text = "No account found in our registry.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { onVerifySuccess("none") }) {
                            AgriText(
                                text = "START REGISTRATION",
                                style = MaterialTheme.typography.labelLarge,
                                color = AgriPrimary,
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
                            val normalized = userRole.lowercase()
                            if (normalized == "none") {
                                showNoAccount = true
                            } else {
                                onVerifySuccess(normalized)
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
                AgriText(
                    text = "RESEND SECURITY CODE",
                    style = MaterialTheme.typography.labelLarge,
                    color = AgriPrimary,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
