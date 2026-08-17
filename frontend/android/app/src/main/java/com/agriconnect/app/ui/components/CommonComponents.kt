package com.agriconnect.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.theme.*
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person

import androidx.compose.material.icons.filled.ArrowBack
import com.agriconnect.app.ui.viewmodel.TranslationViewModel

val LocalTranslationViewModel = staticCompositionLocalOf<TranslationViewModel?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriTopAppBar(
    title: String = "AgriConnect",
    showLogo: Boolean = true,
    hasUnreadNotifications: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showLogo) {
                    Icon(
                        Icons.Default.Eco, 
                        null, 
                        tint = White, 
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                AgriText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = White,
                    letterSpacing = (-0.5).sp
                )
            }
        },
        navigationIcon = {
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = White)
                }
            } else if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                }
            }
        },
        actions = {
            if (onNotificationsClick != null) {
                IconButton(onClick = onNotificationsClick) {
                    BadgedBox(
                        badge = {
                            if (hasUnreadNotifications) {
                                Badge(
                                    containerColor = Color.Red,
                                    modifier = Modifier.size(8.dp).offset(x = (-4).dp, y = 4.dp)
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = White
                        )
                    }
                }
            }
            if (onProfileClick != null) {
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = White)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AgriPrimary
        )
    )
}

@Composable
fun AgriButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = AgriPrimary,
    contentColor: Color = White,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = enabled && !loading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Gray200
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            AgriText(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    prefix: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { AgriText(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) },
            placeholder = { AgriText(placeholder, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) } },
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            readOnly = readOnly,
            enabled = enabled,
            isError = isError,
            prefix = prefix,
            shape = RoundedCornerShape(18.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Gray900, fontWeight = FontWeight.SemiBold),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgriPrimary,
                unfocusedBorderColor = Gray200,
                focusedLabelColor = AgriPrimary,
                unfocusedTextColor = Gray900,
                focusedTextColor = Gray900,
                cursorColor = AgriPrimary,
                focusedContainerColor = White,
                unfocusedContainerColor = White
            )
        )
        if (isError && errorMessage != null) {
            AgriText(
                text = errorMessage,
                color = Error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun AgriCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun AgriSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        if (subtitle != null) {
            AgriText(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                modifier = Modifier.padding(bottom = 2.dp),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AgriText(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Gray900,
                letterSpacing = (-0.5).sp
            )
            if (actionText != null && onActionClick != null) {
                AgriText(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = AgriPrimary,
                    modifier = Modifier.clickable { onActionClick() },
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Surface(
        color = White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Inbox, 
                contentDescription = null, 
                tint = Gray300,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            AgriText(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DashboardStatCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    bgColor: Color, 
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray100),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            AgriText(label, style = MaterialTheme.typography.labelSmall, color = Gray400, fontWeight = FontWeight.Bold)
            AgriText(value, style = MaterialTheme.typography.headlineSmall, color = Gray900, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ProfileListItem(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showChevron: Boolean = true,
    contentColor: Color = Gray900
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AgriSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = AgriPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            AgriText(
                text = label, 
                style = MaterialTheme.typography.bodyLarge, 
                color = contentColor,
                fontWeight = FontWeight.Black
            )
            if (subtitle != null) {
                AgriText(
                    text = subtitle, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Gray500
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Gray300,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AgriText(
    text: String,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    modifier: Modifier = Modifier,
    translationViewModel: com.agriconnect.app.ui.viewmodel.TranslationViewModel? = null
) {
    val vm = translationViewModel ?: LocalTranslationViewModel.current
    
    var translatedText by remember(text, vm?.currentLanguage?.value) { 
        mutableStateOf(text) 
    }

    LaunchedEffect(text, vm?.currentLanguage?.value, vm?.isModelDownloading?.value) {
        if (vm != null && text.isNotEmpty() && vm.isModelDownloading.value == false) {
            vm.translate(text) { result ->
                translatedText = result
            }
        }
    }

    Text(
        text = translatedText,
        style = style,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight,
        letterSpacing = letterSpacing,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
    )
}

@Composable
fun AgriFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .padding(bottom = 16.dp),
            color = Gray200,
            thickness = 2.dp
        )
        
        AgriText(
            text = "AgriConnect",
            style = MaterialTheme.typography.labelLarge,
            color = Gray900,
            fontWeight = FontWeight.Black
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        AgriText(
            text = "MADE BY ZENWE TECHNOLOGIES 2026",
            style = MaterialTheme.typography.labelSmall,
            color = Gray400,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Eco, 
                null, 
                tint = AgriPrimary.copy(alpha = 0.5f), 
                modifier = Modifier.size(16.dp)
            )
            AgriText(
                text = "0% MEDIATOR PROMISE",
                style = MaterialTheme.typography.labelSmall,
                color = AgriPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Black
            )
        }
    }
}
