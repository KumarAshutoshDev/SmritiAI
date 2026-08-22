package com.teamchromium.smritiai.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

data class TextToSpeechState(
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null,
)

@Stable
class SmritiTextToSpeech(
    context: Context,
) {
    var state by mutableStateOf(TextToSpeechState())
        private set

    private val textToSpeech = TextToSpeech(context) { status ->
        handleInitialization(status)
    }

    init {
        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    state = state.copy(isSpeaking = true)
                }

                override fun onDone(utteranceId: String?) {
                    state = state.copy(isSpeaking = false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    state = state.copy(
                        isSpeaking = false,
                        errorMessage = "Spoken playback could not finish.",
                    )
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    state = state.copy(
                        isSpeaking = false,
                        errorMessage = "Spoken playback could not finish.",
                    )
                }
            }
        )
    }

    fun handleInitialization(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            state = state.copy(
                isReady = false,
                errorMessage = "Voice playback is unavailable on this device.",
            )
            return
        }

        val languageStatus = textToSpeech.setLanguage(Locale.getDefault())
        if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
            languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            state = state.copy(
                isReady = false,
                errorMessage = "Voice playback is unavailable in the current language.",
            )
            return
        }

        state = state.copy(
            isReady = true,
            errorMessage = null,
        )
    }

    fun speak(text: String) {
        if (!state.isReady) return

        state = state.copy(errorMessage = null)
        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "smriti-ai-response",
        )
    }

    fun stopSpeaking() {
        textToSpeech.stop()
        state = state.copy(isSpeaking = false)
    }

    fun shutdown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}

@Composable
fun rememberSmritiTextToSpeech(): SmritiTextToSpeech {
    val context = LocalContext.current
    val speaker = remember(context) { SmritiTextToSpeech(context) }

    DisposableEffect(speaker) {
        onDispose {
            speaker.shutdown()
        }
    }

    return speaker
}
