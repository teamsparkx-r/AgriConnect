package com.agriconnect.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMenuClick: (() -> Unit)? = null,
    onBack: () -> Unit,
    onNavigateToLegal: () -> Unit = {},
    onNavigateToRating: () -> Unit = {},
    translationViewModel: com.agriconnect.app.ui.viewmodel.TranslationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLang by translationViewModel.currentLanguage
    
    val languages = listOf(
        "en" to "English",
        "te" to "Telugu"
    )

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = "Settings",
                showLogo = false,
                onMenuClick = onMenuClick,
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
            SettingsGroup(title = "PREFERENCES") {
                ProfileListItem(
                    icon = Icons.Outlined.Language, 
                    label = "Language", 
                    subtitle = if (currentLang == "en") "English" else "Telugu", 
                    onClick = { showLanguageDialog = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                ProfileListItem(icon = Icons.Outlined.Palette, label = "Theme", subtitle = "Light Mode", onClick = { 
                    android.widget.Toast.makeText(context, "Theme selection coming soon", android.widget.Toast.LENGTH_SHORT).show()
                })
                Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                ProfileListItem(icon = Icons.Outlined.Notifications, label = "Notifications", onClick = { 
                    android.widget.Toast.makeText(context, "Notification settings coming soon", android.widget.Toast.LENGTH_SHORT).show()
                })
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { AgriText("Select Language", fontWeight = FontWeight.Black) },
                    text = {
                        Column {
                            languages.forEach { (code, name) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            translationViewModel.setLanguage(code)
                                            showLanguageDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = currentLang == code, onClick = null)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    AgriText(name, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            AgriText("CANCEL", fontWeight = FontWeight.Black)
                        }
                    },
                    containerColor = White,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsGroup(title = "PRIVACY & SECURITY") {
                ProfileListItem(icon = Icons.Outlined.Lock, label = "Change Password", onClick = { 
                    android.widget.Toast.makeText(context, "Feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
                })
                Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                ProfileListItem(icon = Icons.Outlined.VerifiedUser, label = "Privacy Policy", onClick = onNavigateToLegal)
                Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                ProfileListItem(icon = Icons.Outlined.Description, label = "Terms & Conditions", onClick = onNavigateToLegal)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsGroup(title = "GENERAL") {
                ProfileListItem(icon = Icons.Outlined.StarRate, label = "Rate AgriConnect", onClick = onNavigateToRating)
                Divider(modifier = Modifier.padding(horizontal = 24.dp), color = Gray100, thickness = 0.5.dp)
                ProfileListItem(icon = Icons.Outlined.Share, label = "Share App", onClick = { 
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out AgriConnect, the 0% mediator marketplace for farmers: https://agriconnect.app")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                })
            }
            
            AgriFooter()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        AgriText(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Gray400,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
        ) {
            Column {
                content()
            }
        }
    }
}
