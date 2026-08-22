package com.teamchromium.smritiai.screens.ask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val messages = remember { mutableListOf<ChatMessage>() }

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

            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Ask a question about someone you know.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(PatientSpacing.itemGap),
                ) {
                    items(messages) { message ->
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = if (message.isUser) TextAlign.End else TextAlign.Start,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PatientSpacing.itemGap),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Type your question") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .height(PatientTouchTarget.minimum),
                )

                Button(
                    onClick = {
                        val text = input.trim()
                        if (text.isNotEmpty()) {
                            messages.add(ChatMessage(text, isUser = true))
                            messages.add(ChatMessage("I'll answer here once connected.", isUser = false))
                            input = ""
                        }
                    },
                    modifier = Modifier
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text(
                        text = "Send",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
