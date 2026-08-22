package com.teamchromium.smritiai.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teamchromium.smritiai.screens.addmemory.AddMemoryScreen
import com.teamchromium.smritiai.screens.addperson.AddPersonScreen
import com.teamchromium.smritiai.screens.ask.AskSmritiScreen
import com.teamchromium.smritiai.screens.consent.ConsentScreen
import com.teamchromium.smritiai.screens.home.HomeScreen
import com.teamchromium.smritiai.screens.memoryhistory.MemoryHistoryScreen
import com.teamchromium.smritiai.screens.recognize.RecognizePersonScreen
import com.teamchromium.smritiai.screens.unknownperson.UnknownPersonScreen
import com.teamchromium.smritiai.security.ConsentManager

private object AppRoute {
    const val Consent = "consent"
    const val Shell = "shell"
    const val Ask = "ask"
    const val Recognize = "recognize"
    const val UnknownPerson = "unknownperson"
    const val AddPerson = "addperson"
    const val AddMemory = "addmemory"
    const val MemoryHistory = "memoryhistory"
}

@Composable
fun SmritiNavGraph(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val startDestination = if (ConsentManager.checkConsent(context)) {
        AppRoute.Shell
    } else {
        AppRoute.Consent
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(AppRoute.Consent) {
                ConsentScreen(
                    onConsentAccepted = {
                        ConsentManager.setConsentAccepted(context, true)
                        navController.navigate(AppRoute.Shell) {
                            popUpTo(AppRoute.Consent) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppRoute.Shell) {
                HomeScreen(
                    onAskSmriti = { navController.navigate(AppRoute.Ask) },
                    onRecognize = { navController.navigate(AppRoute.Recognize) },
                    onAddMemory = { navController.navigate(AppRoute.AddMemory) },
                    onMemoryHistory = { navController.navigate(AppRoute.MemoryHistory) },
                )
            }
            composable(AppRoute.Ask) {
                AskSmritiScreen()
            }
                        composable(AppRoute.Recognize) {
                RecognizePersonScreen(
                    onNoMatch = { navController.navigate(AppRoute.UnknownPerson) },
                )
            }
            composable(AppRoute.UnknownPerson) {
                UnknownPersonScreen(
                    onAddPerson = { navController.navigate(AppRoute.AddPerson) },
                    onSkip = { navController.popBackStack() },
                )
            }
            composable(AppRoute.AddPerson) {
                AddPersonScreen(
                    onGoToConsent = { navController.navigate(AppRoute.Consent) },
                    onPersonSaved = { navController.popBackStack() },
                )
            }
            composable(AppRoute.AddMemory) {
                AddMemoryScreen()
            }
            composable(AppRoute.MemoryHistory) {
                MemoryHistoryScreen()
            }
        }
    }
}