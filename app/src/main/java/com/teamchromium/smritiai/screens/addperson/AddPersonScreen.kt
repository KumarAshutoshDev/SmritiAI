package com.teamchromium.smritiai.screens.addperson

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.teamchromium.smritiai.security.ConsentManager
import com.teamchromium.smritiai.ui.theme.PatientSpacing
import com.teamchromium.smritiai.ui.theme.PatientTouchTarget
import com.teamchromium.smritiai.ui.theme.SmritiSurface
import java.io.File
import java.io.IOException
import android.graphics.Bitmap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import com.teamchromium.smritiai.data.local.DatabaseProvider
import com.teamchromium.smritiai.data.local.IdentityEntity
import com.teamchromium.smritiai.recognition.EmbeddingExtractor
import com.teamchromium.smritiai.recognition.FaceDetectorHelper
import com.teamchromium.smritiai.recognition.toBitmap
import kotlinx.coroutines.launch

@Composable
fun AddPersonScreen(
    onGoToConsent: () -> Unit,
    onPersonSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val consentGranted = ConsentManager.checkConsent(context)

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
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var captureStatus by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var voiceNotePath by remember { mutableStateOf<String?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }

    DisposableEffect(lifecycleOwner, consentGranted, hasCameraPermission) {
        if (consentGranted && hasCameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    captureStatus = "Camera error"
                }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose {
            mediaRecorder?.apply {
                try { stop() } catch (_: Exception) {}
                try { release() } catch (_: Exception) {}
            }
        }
    }

    fun startRecording() {
        val file = File(context.cacheDir, "voice_note_${System.currentTimeMillis()}.m4a")
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
            mediaRecorder = recorder
            voiceNotePath = file.absolutePath
            isRecording = true
        } catch (e: IOException) {
            isRecording = false
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            try { stop() } catch (_: Exception) {}
            try { release() } catch (_: Exception) {}
        }
        mediaRecorder = null
        isRecording = false
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
                text = "Add Person",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )

            if (!consentGranted) {
                Text(
                    text = "Consent is required before using the camera or microphone.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onGoToConsent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text("Go to Consent", style = MaterialTheme.typography.labelLarge)
                }
            } else if (!hasCameraPermission) {
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text("Grant Camera Permission", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                )

                Button(
                    onClick = {
                        captureStatus = null
                        val executor = ContextCompat.getMainExecutor(context)
                        imageCapture.takePicture(
                            executor,
                                                        object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    capturedBitmap = image.toBitmap()
                                    captureStatus = if (capturedBitmap != null) {
                                        "Face photo captured"
                                    } else {
                                        "Could not process photo, please try again"
                                    }
                                    image.close()
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    captureStatus = "Capture failed"
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text("Capture Face Photo", style = MaterialTheme.typography.labelLarge)
                }

                captureStatus?.let {
                    Text(it, style = MaterialTheme.typography.bodyLarge)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                )

                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                )

                Spacer(modifier = Modifier.height(PatientSpacing.itemGap))

                Text(
                    text = "Optional Voice Note",
                    style = MaterialTheme.typography.titleLarge,
                )

                if (!hasAudioPermission) {
                    Button(
                        onClick = { audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PatientTouchTarget.minimum),
                    ) {
                        Text("Grant Microphone Permission", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Button(
                        onClick = {
                            if (isRecording) stopRecording() else startRecording()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(PatientTouchTarget.minimum),
                    ) {
                        Text(
                            if (isRecording) "Stop Recording" else "Record Voice Note",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    voiceNotePath?.let {
                        Text(
                            "Voice note saved: ${File(it).name}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PatientSpacing.itemGap))

                val canSave = capturedBitmap != null &&
                    name.isNotBlank() &&
                    relationship.isNotBlank() &&
                    !isSaving

                Button(
                    onClick = {
                        val bitmap = capturedBitmap ?: return@Button
                        isSaving = true
                        saveError = null
                        coroutineScope.launch {
                            try {
                                val faces = FaceDetectorHelper.detectFacesInBitmap(bitmap)
                                if (faces.isEmpty()) {
                                    saveError = "No face found in photo, please retake it"
                                    isSaving = false
                                    return@launch
                                }
                                val embedding = EmbeddingExtractor.extractEmbedding(
                                    context,
                                    bitmap,
                                    faces[0].boundingBox
                                )
                                val identity = IdentityEntity(
                                    name = name,
                                    relationship = relationship,
                                    faceEmbedding = embedding
                                )
                                val identityDao = DatabaseProvider.getDatabase(context).identityDao()
                                identityDao.insert(identity)
                                isSaving = false
                                onPersonSaved()
                            } catch (e: Exception) {
                                saveError = "Could not save, please try again"
                                isSaving = false
                            }
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    } else {
                        Text("Save Person", style = MaterialTheme.typography.labelLarge)
                    }
                }

                saveError?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
