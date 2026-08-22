package com.teamchromium.smritiai.screens.unknownperson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.teamchromium.smritiai.ui.theme.PatientSpacing
import com.teamchromium.smritiai.ui.theme.PatientTouchTarget
import com.teamchromium.smritiai.ui.theme.SmritiSurface

@Composable
fun UnknownPersonScreen(
    onAddPerson: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = "We don't recognize this person yet",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = onAddPerson,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PatientTouchTarget.minimum),
            ) {
                Text("Add Person", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PatientTouchTarget.minimum),
            ) {
                Text("Skip", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}