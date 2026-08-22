package com.teamchromium.smritiai.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.teamchromium.smritiai.ui.theme.PatientSpacing
import com.teamchromium.smritiai.ui.theme.PatientTouchTarget
import com.teamchromium.smritiai.ui.theme.SmritiOnPrimary
import com.teamchromium.smritiai.ui.theme.SmritiPrimary
import com.teamchromium.smritiai.ui.theme.SmritiSurface

@Composable
fun HomeScreen(
    onAskSmriti: () -> Unit,
    onRecognize: () -> Unit,
    onAddMemory: () -> Unit,
    onMemoryHistory: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
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
                text = "SmritiAI",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            HomeActionButton(
                text = "Ask Smriti AI",
                onClick = onAskSmriti,
            )
            HomeActionButton(
                text = "Recognize Person",
                onClick = onRecognize,
            )
            HomeActionButton(
                text = "Add Memory",
                onClick = onAddMemory,
            )
            HomeActionButton(
                text = "Memory History",
                onClick = onMemoryHistory,
            )
        }
    }
}

@Composable
private fun HomeActionButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(PatientTouchTarget.minimum),
        colors = ButtonDefaults.buttonColors(
            containerColor = SmritiPrimary,
            contentColor = SmritiOnPrimary,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}
