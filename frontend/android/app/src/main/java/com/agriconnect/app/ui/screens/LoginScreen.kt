package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun LoginScreen(
    role: String, 
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit, 
    onSignupClick: () -> Unit
) {
    var mobileNumber by remember { mutableStateOf("") }
    val loading by viewModel.loading
    val error by viewModel.error

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
            text = "Welcome to AgriConnect",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "DIRECT FARMER & MERCHANT MARKETPLACE",
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        AgriTextField(
            value = mobileNumber,
            onValueChange = { if (it.length <= 10) mobileNumber = it },
            label = "10-Digit Mobile Number",
            prefix = { Text("+91 ", style = MaterialTheme.typography.bodyLarge) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = error != null,
            errorMessage = error
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        AgriButton(
            text = "Send OTP",
            onClick = { 
                viewModel.sendOtp(mobileNumber) {
                    onLoginSuccess(mobileNumber)
                }
            },
            loading = loading,
            enabled = mobileNumber.length == 10
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onSignupClick) {
            Text(
                text = "INITIALIZE NEW REGISTRY ACCOUNT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "AgriConnect Node Identity Protection System",
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
