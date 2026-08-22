package com.teamchromium.smritiai.speech

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

data class SpeechRecognitionState(
    val transcript: String = "",
    val partialTranscript: String = "",
    val isListening: Boolean = false,
    val errorMessage: String? = null,
)

@Stable
class SmritiSpeechRecognizer(
    private val speechRecognizer: SpeechRecognizer,
) {
    var state by mutableStateOf(SpeechRecognitionState())
        private set

    private val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
    }

    init {
        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    state = state.copy(isListening = true, errorMessage = null)
                }

                override fun onBeginningOfSpeech() = Unit

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    state = state.copy(isListening = false)
                }

                override fun onError(error: Int) {
                    state = state.copy(
                        isListening = false,
                        errorMessage = error.toSpeechErrorMessage(),
                    )
                }

                override fun onResults(results: Bundle?) {
                    val transcript = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()

                    state = state.copy(
                        transcript = transcript,
                        partialTranscript = "",
                        isListening = false,
                        errorMessage = null,
                    )
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialTranscript = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()

                    state = state.copy(partialTranscript = partialTranscript)
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            },
        )
    }

    fun startListening() {
        state = state.copy(
            transcript = "",
            partialTranscript = "",
            errorMessage = null,
        )
        speechRecognizer.startListening(recognitionIntent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        state = state.copy(isListening = false)
    }

    fun cancel() {
        speechRecognizer.cancel()
        state = state.copy(isListening = false)
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}

@Composable
fun rememberSmritiSpeechRecognizer(): SmritiSpeechRecognizer {
    val context = LocalContext.current
    val recognizer = remember {
        SmritiSpeechRecognizer(SpeechRecognizer.createSpeechRecognizer(context))
    }

    DisposableEffect(recognizer) {
        onDispose {
            recognizer.destroy()
        }
    }

    return recognizer
}

private fun Int.toSpeechErrorMessage(): String {
    return when (this) {
        SpeechRecognizer.ERROR_AUDIO -> "The microphone could not capture audio."
        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition stopped unexpectedly."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed for speech recognition."
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        -> "Speech recognition needs the device speech service to be available."
        SpeechRecognizer.ERROR_NO_MATCH -> "No clear speech was recognized."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is already listening."
        SpeechRecognizer.ERROR_SERVER -> "The device speech service could not respond."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was heard."
        SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "Speech recognition is receiving too many requests."
        else -> "Speech recognition could not complete."
    }
}
