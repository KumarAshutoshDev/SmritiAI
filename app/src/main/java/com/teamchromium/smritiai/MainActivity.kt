package com.teamchromium.smritiai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.teamchromium.smritiai.navigation.SmritiNavGraph
import com.teamchromium.smritiai.ui.theme.SmritiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmritiTheme {
                SmritiNavGraph()
            }
        }
    }
}
