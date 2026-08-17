package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
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
import com.agriconnect.app.ui.viewmodel.AuthViewModel
import androidx.compose.foundation.text.KeyboardOptions
import com.agriconnect.app.ui.theme.AgriPrimary

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
            .background(MaterialTheme.colorScheme.background),
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
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                AgriText(
                    text = "Welcome to AgriConnect",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                AgriText(
                    text = "DIRECT FARMER & MERCHANT MARKETPLACE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            AgriTextField(
                value = mobileNumber,
                onValueChange = { if (it.length <= 10) mobileNumber = it },
                label = "10-Digit Mobile Number",
                prefix = { AgriText("+91 ", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black) },
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
                AgriText(
                    text = "INITIALIZE NEW REGISTRY ACCOUNT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = AgriPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            
            AgriText(
                text = "AgriConnect Node Identity Protection System",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
