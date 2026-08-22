package com.teamchromium.smritiai.screens.ask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.teamchromium.smritiai.network.ConnectivityStatus
import com.teamchromium.smritiai.network.rememberConnectivityStatus
import com.teamchromium.smritiai.ui.theme.PatientSpacing
import com.teamchromium.smritiai.ui.theme.PatientTouchTarget
import com.teamchromium.smritiai.ui.theme.SmritiSurface

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
)

@Composable
fun AskSmritiScreen(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    val messages = remember { androidx.compose.runtime.mutableStateListOf<ChatMessage>() }
    val connectivityStatus by rememberConnectivityStatus()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SmritiSurface,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PatientSpacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(PatientSpacing.itemGap),
        ) {
            Text(
                text = "Ask Smriti AI",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            ConnectivityStateCard(
                connectivityStatus = connectivityStatus,
                modifier = Modifier.fillMaxWidth(),
            )

            ConversationPanel(
                messages = messages,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PatientSpacing.itemGap),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Type your question") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                    enabled = connectivityStatus == ConnectivityStatus.Online,
                )

                Button(
                    onClick = {
                        val text = input.trim()
                        if (text.isNotEmpty() && connectivityStatus == ConnectivityStatus.Online) {
                            messages.add(ChatMessage(text = text, isUser = true))
                            messages.add(
                                ChatMessage(
                                    text = "I heard your question. The assistant reply will appear here once the live response service is connected.",
                                    isUser = false,
                                )
                            )
                            input = ""
                        }
                    },
                    enabled = connectivityStatus == ConnectivityStatus.Online && input.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text(
                        text = if (connectivityStatus == ConnectivityStatus.Online) {
                            "Ask Smriti AI"
                        } else {
                            "Needs Connectivity"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectivityStateCard(
    connectivityStatus: ConnectivityStatus,
    modifier: Modifier = Modifier,
) {
    val cardColors = if (connectivityStatus == ConnectivityStatus.Online) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    }

    Card(
        modifier = modifier,
        colors = cardColors,
    ) {
        Column(
            modifier = Modifier.padding(PatientSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(PatientSpacing.contentGap),
        ) {
            Text(
                text = if (connectivityStatus == ConnectivityStatus.Online) {
                    "Ready to answer"
                } else {
                    "Needs connectivity"
                },
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = if (connectivityStatus == ConnectivityStatus.Online) {
                    "Ask a question about someone you know. The assistant can answer when the device is online."
                } else {
                    "Ask Smriti AI needs internet access for answers. Recognize Person and saved memories still work offline."
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ConversationPanel(
    messages: SnapshotStateList<ChatMessage>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PatientSpacing.cardPadding),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Ask a question with one clear thought at a time. SmritiAI will show each exchange here.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PatientSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(PatientSpacing.itemGap),
            ) {
                items(messages) { message ->
                    Card(
                        colors = if (message.isUser) {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        } else {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                    ) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = if (message.isUser) TextAlign.End else TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PatientSpacing.cardPadding),
                        )
                    }
                }
            }
        }
    }
}
