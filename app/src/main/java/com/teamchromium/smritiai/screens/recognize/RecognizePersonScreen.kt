package com.teamchromium.smritiai.screens.recognize

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.teamchromium.smritiai.data.local.DatabaseProvider
import com.teamchromium.smritiai.recognition.EmbeddingExtractor
import com.teamchromium.smritiai.recognition.FaceDetectorHelper
import com.teamchromium.smritiai.recognition.FaceMatcher
import com.teamchromium.smritiai.recognition.MatchResult
import com.teamchromium.smritiai.recognition.toUprightBitmap
import com.teamchromium.smritiai.security.ConsentManager
import com.teamchromium.smritiai.ui.theme.PatientSpacing
import com.teamchromium.smritiai.ui.theme.PatientTouchTarget
import com.teamchromium.smritiai.ui.theme.SmritiSurface
import kotlinx.coroutines.launch

@Composable
fun RecognizePersonScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val faceMatcher = remember {
        FaceMatcher(DatabaseProvider.getDatabase(context).identityDao())
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val hasConsent = remember { ConsentManager.checkConsent(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var recognitionState by remember {
        mutableStateOf<RecognitionUiState>(RecognitionUiState.Idle)
    }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner, hasCameraPermission, hasConsent) {
        if (hasCameraPermission && hasConsent) {
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
                        imageCapture
                    )
                } catch (_: Exception) {
                    recognitionState = RecognitionUiState.Error("The camera could not start.")
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
                .padding(PatientSpacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(PatientSpacing.itemGap),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Recognize Person",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )

            if (!hasCameraPermission) {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text(
                        text = "Grant Camera Permission",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            } else if (!hasConsent) {
                Text(
                    text = "Consent is required before using the camera. Please complete the consent step first.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            } else {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                Button(
                    onClick = {
                        recognitionState = RecognitionUiState.Matching
                        val executor = ContextCompat.getMainExecutor(context)
                        imageCapture.takePicture(
                            executor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                @ExperimentalGetImage
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    coroutineScope.launch {
                                        recognitionState = image.resolveRecognitionState(
                                            context = context,
                                            faceMatcher = faceMatcher,
                                        )
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    recognitionState = RecognitionUiState.Error("The photo could not be captured.")
                                }
                            }
                        )
                    },
                    enabled = recognitionState !is RecognitionUiState.Matching,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PatientTouchTarget.minimum),
                ) {
                    Text(
                        text = if (recognitionState is RecognitionUiState.Matching) {
                            "Checking Match..."
                        } else {
                            "Capture"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                RecognitionStateCard(
                    state = recognitionState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun RecognitionStateCard(
    state: RecognitionUiState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        RecognitionUiState.Idle -> Unit
        RecognitionUiState.Matching -> RecognitionMessageCard(
            title = "Matching in progress",
            message = "Please hold still while SmritiAI checks the captured face on-device.",
            modifier = modifier,
        )
        is RecognitionUiState.Matched -> Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(PatientSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(PatientSpacing.contentGap),
            ) {
                Text(
                    text = "Match found",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.name,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Relationship: ${state.relationship}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Confidence: ${state.confidencePercent}%",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        is RecognitionUiState.NoFace -> RecognitionMessageCard(
            title = "No face found",
            message = state.message,
            modifier = modifier,
        )
        is RecognitionUiState.Unknown -> RecognitionMessageCard(
            title = "No saved match",
            message = state.message,
            modifier = modifier,
        )
        is RecognitionUiState.Error -> RecognitionMessageCard(
            title = "Recognition unavailable",
            message = state.message,
            modifier = modifier,
        )
    }
}

@Composable
private fun RecognitionMessageCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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

@ExperimentalGetImage
private suspend fun ImageProxy.resolveRecognitionState(
    context: android.content.Context,
    faceMatcher: FaceMatcher,
): RecognitionUiState {
    return try {
        val bitmap = toUprightBitmap()
            ?: return RecognitionUiState.Error("The captured photo could not be processed.")
        val faces = FaceDetectorHelper.detectFaces(this)
        if (faces.isEmpty()) {
            RecognitionUiState.NoFace("Try again with one face centered in the camera frame.")
        } else {
            val embedding = EmbeddingExtractor.extractEmbedding(
                context = context,
                faceBitmap = bitmap,
                faceBounds = faces.first().boundingBox,
            )
            when (val match = faceMatcher.findBestMatch(embedding)) {
                is MatchResult.Found -> RecognitionUiState.Matched(
                    name = match.identity.name,
                    relationship = match.identity.relationship,
                    confidencePercent = (match.confidence * 100).toInt(),
                )
                MatchResult.NotFound -> RecognitionUiState.Unknown(
                    "SmritiAI could not match this face to a saved person yet."
                )
            }
        }
    } catch (_: Exception) {
        RecognitionUiState.Error("Recognition could not finish. Please try again.")
    } finally {
        close()
    }
}

private sealed interface RecognitionUiState {
    data object Idle : RecognitionUiState
    data object Matching : RecognitionUiState
    data class Matched(
        val name: String,
        val relationship: String,
        val confidencePercent: Int,
    ) : RecognitionUiState
    data class NoFace(val message: String) : RecognitionUiState
    data class Unknown(val message: String) : RecognitionUiState
    data class Error(val message: String) : RecognitionUiState
}
