package com.teamchromium.smritiai.screens.addmemory

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.teamchromium.smritiai.data.local.BehaviorEntity
import com.teamchromium.smritiai.data.local.DatabaseProvider
import com.teamchromium.smritiai.data.local.IdentityEntity
import com.teamchromium.smritiai.security.ConsentManager
import com.teamchromium.smritiai.speech.rememberSmritiSpeechRecognizer
import com.teamchromium.smritiai.ui.theme.PatientSpacing
import com.teamchromium.smritiai.ui.theme.PatientTouchTarget
import com.teamchromium.smritiai.ui.theme.SmritiSurface
import kotlinx.coroutines.launch

@Composable
fun AddMemoryScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val consentGranted = ConsentManager.checkConsent(context)
    val coroutineScope = rememberCoroutineScope()
    val speechRecognizer = rememberSmritiSpeechRecognizer()
    val speechState = speechRecognizer.state

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasAudioPermission = granted
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var captureStatus by remember { mutableStateOf<String?>(null) }
    var hasCapturedPhoto by remember { mutableStateOf(false) }
    var contacts by remember { mutableStateOf<List<IdentityEntity>>(emptyList()) }
    var selectedContactId by remember { mutableStateOf<Long?>(null) }
    var contactLoadError by remember { mutableStateOf<String?>(null) }
    var noteTranscript by remember { mutableStateOf("") }
    var saveStatus by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            contacts = DatabaseProvider.getDatabase(context).identityDao().getAll()
            contactLoadError = null
        } catch (exception: Exception) {
            contactLoadError = "Could not load saved contacts."
        }
    }

    LaunchedEffect(speechState.transcript) {
        if (speechState.transcript.isNotBlank()) {
            noteTranscript = speechState.transcript
        }
    }

    DisposableEffect(lifecycleOwner, consentGranted, hasCameraPermission) {
        if (consentGranted && hasCameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                    )
                } catch (exception: Exception) {
                    captureStatus = "Camera error"
                }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose { }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = SmritiSurface,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PatientSpacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(PatientSpacing.itemGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Add Memory",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )

            if (!consentGranted) {
                Spacer(modifier = Modifier.height(PatientSpacing.itemGap))
                Text(
                    text = "Consent is required before using the camera or microphone.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            } else if (!hasCameraPermission) {
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text(
                        text = "Grant Camera Permission",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else {
                StepCard(
                    title = "Step 1: Capture memory photo",
                    message = "Take one photo for this memory before adding the contact and note.",
                )

                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )

                Button(
                    onClick = {
                        captureStatus = null
                        val executor = ContextCompat.getMainExecutor(context)
                        imageCapture.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    image.close()
                                    hasCapturedPhoto = true
                                    captureStatus = "Memory photo captured"
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    hasCapturedPhoto = false
                                    captureStatus = "Capture failed"
                                }
                            },
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text(
                        text = "Capture Memory Photo",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                captureStatus?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                StepCard(
                    title = "Step 2: Link to a contact",
                    message = "Choose the person this memory is about. The memory stores only the contact ID.",
                )

                ContactPicker(
                    contacts = contacts,
                    selectedContactId = selectedContactId,
                    contactLoadError = contactLoadError,
                    onSelectContact = { selectedContactId = it },
                )

                StepCard(
                    title = "Step 3: Record memory note",
                    message = buildNoteStatusMessage(
                        isListening = speechState.isListening,
                        partialTranscript = speechState.partialTranscript,
                        speechError = speechState.errorMessage,
                        noteTranscript = noteTranscript,
                    ),
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
                            if (speechState.isListening) {
                                speechRecognizer.stopListening()
                            } else {
                                speechRecognizer.startListening()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PatientTouchTarget.minimum),
                    ) {
                        Text(
                            text = if (speechState.isListening) {
                                "Stop Recording Note"
                            } else {
                                "Record Memory Note"
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                SaveMemoryButton(
                    enabled = hasCapturedPhoto &&
                        noteTranscript.isNotBlank() &&
                        !speechState.isListening &&
                        !isSaving,
                    isSaving = isSaving,
                    onClick = {
                        val transcript = noteTranscript.trim()
                        if (transcript.isNotBlank()) {
                            isSaving = true
                            saveStatus = null
                            coroutineScope.launch {
                                try {
                                    DatabaseProvider.getDatabase(context).behaviorDao().insert(
                                        BehaviorEntity(
                                            contactId = selectedContactId,
                                            photoRef = null,
                                            audioRef = null,
                                            transcript = transcript,
                                            aiSummary = null,
                                        ),
                                    )
                                    saveStatus = "Memory saved"
                                    noteTranscript = ""
                                    hasCapturedPhoto = false
                                    captureStatus = null
                                } catch (exception: Exception) {
                                    saveStatus = "Could not save memory, please try again."
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    },
                )

                saveStatus?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (it == "Memory saved") {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactPicker(
    contacts: List<IdentityEntity>,
    selectedContactId: Long?,
    contactLoadError: String?,
    onSelectContact: (Long?) -> Unit,
) {
    contactLoadError?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
    }

    if (contacts.isEmpty() && contactLoadError == null) {
        Text(
            text = "No saved contacts yet. You can still save this memory without a contact.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    } else {
        contacts.forEach { contact ->
            Button(
                onClick = { onSelectContact(contact.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PatientTouchTarget.minimum),
            ) {
                Text(
                    text = if (selectedContactId == contact.id) {
                        "Selected: ${contact.name}, ${contact.relationship}"
                    } else {
                        "${contact.name}, ${contact.relationship}"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    Button(
        onClick = { onSelectContact(null) },
        modifier = Modifier
            .fillMaxWidth()
            .height(PatientTouchTarget.minimum),
    ) {
        Text(
            text = if (selectedContactId == null) {
                "Selected: No Contact"
            } else {
                "Save Without Contact"
            },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun StepCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
private fun SaveMemoryButton(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(PatientTouchTarget.minimum),
    ) {
        if (isSaving) {
            CircularProgressIndicator(modifier = Modifier.height(24.dp))
        } else {
            Text(
                text = "Save Memory",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

private fun buildNoteStatusMessage(
    isListening: Boolean,
    partialTranscript: String,
    speechError: String?,
    noteTranscript: String,
): String {
    return when {
        isListening && partialTranscript.isNotBlank() -> partialTranscript
        isListening -> "Listening now. Speak the memory clearly."
        speechError != null -> speechError
        noteTranscript.isNotBlank() -> noteTranscript
        else -> "Use the microphone to dictate the memory note. Only the text transcript is saved."
    }
}
