package com.saha.androidfm.utils.ext

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShowToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(LocalContext.current, message, duration).show()
}

// Get context extension
@Composable
fun getContext(): Context {
    return LocalContext.current
}

// Show long toast
@Composable
fun ShowLongToast(message: String) {
    Toast.makeText(LocalContext.current, message, Toast.LENGTH_LONG).show()
}

// Network check extension
@Composable
fun isNetworkAvailable(): Boolean {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null && (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            )
}