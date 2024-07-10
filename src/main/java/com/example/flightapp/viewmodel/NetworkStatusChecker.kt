package com.example.flightapp.viewmodel

import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utility class to check the network status.
 * It uses the ConnectivityManager to get the current network capabilities.
 */
class NetworkStatusChecker(private val connectivityManager: ConnectivityManager) {

    /**
     * Utility class to check the network status.
     * It uses the ConnectivityManager to get the current network capabilities.
     */
    fun hasInternetConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Checks if there is an active WiFi connection.
     * @return true if there is a WiFi connection, false otherwise.
     */
    fun hasWifiConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
