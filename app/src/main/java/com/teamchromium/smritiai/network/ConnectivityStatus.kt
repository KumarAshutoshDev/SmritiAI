package com.teamchromium.smritiai.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService

enum class ConnectivityStatus {
    Online,
    Offline,
}

@Composable
fun rememberConnectivityStatus(): State<ConnectivityStatus> {
    val context = LocalContext.current
    val connectivityManager = remember(context) {
        context.getSystemService<ConnectivityManager>()
    }
    val statusState = remember {
        mutableStateOf(connectivityManager.currentConnectivityStatus())
    }

    DisposableEffect(connectivityManager) {
        if (connectivityManager == null) {
            statusState.value = ConnectivityStatus.Offline
            onDispose { }
        } else {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    statusState.value = connectivityManager.currentConnectivityStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities,
                ) {
                    statusState.value = connectivityManager.currentConnectivityStatus()
                }

                override fun onLost(network: Network) {
                    statusState.value = connectivityManager.currentConnectivityStatus()
                }
            }

            connectivityManager.registerDefaultNetworkCallback(callback)
            onDispose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }

    return statusState
}

private fun ConnectivityManager?.currentConnectivityStatus(): ConnectivityStatus {
    if (this == null) return ConnectivityStatus.Offline

    val capabilities = getNetworkCapabilities(activeNetwork) ?: return ConnectivityStatus.Offline
    val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    return if (hasInternet && isValidated) {
        ConnectivityStatus.Online
    } else {
        ConnectivityStatus.Offline
    }
}
