package com.agriconnect.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.AssistantState

@Composable
fun VoiceAssistantOverlay(
    state: AssistantState,
    transcribedText: String,
    responseText: String,
    requiresConfirmation: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onStop: () -> Unit = {}
) {
    if (state == AssistantState.IDLE) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(enabled = state != AssistantState.LISTENING) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when(state) {
                            AssistantState.LISTENING -> "Listening..."
                            AssistantState.TRANSCRIBING -> "Transcribing..."
                            AssistantState.THINKING -> "Thinking..."
                            AssistantState.RESPONDING -> "Assistant"
                            AssistantState.ERROR -> "Error"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = Gray400,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Gray400)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (transcribedText.isNotEmpty()) {
                    Text(
                        text = "\"$transcribedText\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray900,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state == AssistantState.LISTENING || state == AssistantState.THINKING) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape),
                        color = AgriPrimary,
                        trackColor = AgriSecondary
                    )
                }

                if (state == AssistantState.LISTENING) {
                    Text(
                        text = "Tap the button when you're done speaking",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray400,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStop,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.size(64.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Filled.Stop, null, tint = White, modifier = Modifier.size(32.dp))
                    }
                }

                if (responseText.isNotEmpty()) {
                    Surface(
                        color = AgriSecondary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AgriText(
                            text = responseText,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgriPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (state == AssistantState.RESPONDING && requiresConfirmation) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            AgriText("Cancel", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriPrimary)
                        ) {
                            AgriText("Confirm", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
