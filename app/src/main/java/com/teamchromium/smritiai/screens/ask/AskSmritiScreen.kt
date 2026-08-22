package com.teamchromium.smritiai.screens.ask

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.teamchromium.smritiai.data.local.DatabaseProvider
import com.teamchromium.smritiai.intelligence.ContextAssembler
import com.teamchromium.smritiai.intelligence.LlmService
import com.teamchromium.smritiai.network.ConnectivityStatus
import com.teamchromium.smritiai.network.rememberConnectivityStatus
import com.teamchromium.smritiai.speech.rememberSmritiSpeechRecognizer
import com.teamchromium.smritiai.speech.rememberSmritiTextToSpeech
import com.teamchromium.smritiai.ui.theme.PatientSpacing
import com.teamchromium.smritiai.ui.theme.PatientTouchTarget
import com.teamchromium.smritiai.ui.theme.SmritiSurface
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
)

@Composable
fun AskSmritiScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val speechRecognizer = rememberSmritiSpeechRecognizer()
    val textToSpeech = rememberSmritiTextToSpeech()
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val connectivityStatus by rememberConnectivityStatus()
    val coroutineScope = rememberCoroutineScope()
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasAudioPermission = granted
    }

    val recognitionState = speechRecognizer.state
    val ttsState = textToSpeech.state
    var assistantLatencyMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(recognitionState.transcript) {
        if (recognitionState.transcript.isNotBlank()) {
            input = recognitionState.transcript
        }
    }

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

            VoiceStatusCard(
                isListening = recognitionState.isListening,
                partialTranscript = recognitionState.partialTranscript,
                speechError = recognitionState.errorMessage,
                isSpeaking = ttsState.isSpeaking,
                ttsError = ttsState.errorMessage,
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

                if (!hasAudioPermission) {
                    Button(
                        onClick = { audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PatientTouchTarget.minimum),
                    ) {
                        Text(
                            text = "Grant Microphone Permission",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (recognitionState.isListening) {
                                speechRecognizer.stopListening()
                            } else if (connectivityStatus == ConnectivityStatus.Online) {
                                speechRecognizer.startListening()
                            }
                        },
                        enabled = connectivityStatus == ConnectivityStatus.Online,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PatientTouchTarget.minimum),
                    ) {
                        Text(
                            text = if (recognitionState.isListening) {
                                "Stop Listening"
                            } else if (connectivityStatus == ConnectivityStatus.Online) {
                                "Ask With Your Voice"
                            } else {
                                "Voice Needs Connectivity"
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Button(
                    onClick = {
                        val text = input.trim()
                        if (text.isNotEmpty() && connectivityStatus == ConnectivityStatus.Online) {
                            messages.add(ChatMessage(text = text, isUser = true))
                            input = ""
                            if (recognitionState.isListening) {
                                speechRecognizer.stopListening()
                            }

                            val startTime = System.currentTimeMillis()

                            coroutineScope.launch {
                                val db = DatabaseProvider.getDatabase(context)
                                val contextAssembler = ContextAssembler(
                                    identityDao = db.identityDao(),
                                    behaviorDao = db.behaviorDao(),
                                )
                                val llmContext = contextAssembler.assembleContext(recognizedContactId = null)
                                val reply = LlmService.askQuestion(llmContext, text)

                                val latencyMs = System.currentTimeMillis() - startTime
                                assistantLatencyMs = latencyMs

                                messages.add(ChatMessage(text = reply, isUser = false))

                                if (ttsState.isReady) {
                                    textToSpeech.speak(reply)
                                }
                            }
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

                assistantLatencyMs?.let { latencyMs ->
                    Text(
                        text = "Assistant latency: ${latencyMs}ms",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                if (ttsState.isSpeaking) {
                    OutlinedButton(
                        onClick = { textToSpeech.stopSpeaking() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PatientTouchTarget.minimum),
                    ) {
                        Text(
                            text = "Stop Spoken Answer",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
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
private fun VoiceStatusCard(
    isListening: Boolean,
    partialTranscript: String,
    speechError: String?,
    isSpeaking: Boolean,
    ttsError: String?,
    modifier: Modifier = Modifier,
) {
    val title = when {
        isSpeaking -> "Speaking answer aloud"
        isListening -> "Listening to your question"
        else -> "Voice-first assistant"
    }

    val message = when {
        isSpeaking -> "SmritiAI is reading the assistant response aloud. The text below is only a confirmation copy."
        isListening && partialTranscript.isNotBlank() -> partialTranscript
        isListening -> "Speak clearly. Your words will appear here as they are recognized."
        speechError != null -> speechError
        ttsError != null -> ttsError
        else -> "You can type or use the microphone, but every assistant reply is spoken by default."
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(PatientSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(PatientSpacing.contentGap),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = message,
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
