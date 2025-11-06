package za.co.rosebankcollege.st10304152.taskmaster.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Network state manager to detect online/offline status
 * Provides real-time network connectivity information
 */
class NetworkStateManager(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Get current network state as Flow
     */
    fun getNetworkState(): Flow<Boolean> = callbackFlow {
        var callbackRegistered = false
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                try {
                    trySend(true)
                } catch (e: ClosedSendChannelException) {
                    // Channel is closed - ignore
                } catch (e: Exception) {
                    // Other exceptions - ignore to prevent crashes
                }
            }
            
            override fun onLost(network: Network) {
                try {
                    trySend(false)
                } catch (e: ClosedSendChannelException) {
                    // Channel is closed - ignore
                } catch (e: Exception) {
                    // Other exceptions - ignore to prevent crashes
                }
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                try {
                    val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    trySend(isConnected)
                } catch (e: ClosedSendChannelException) {
                    // Channel is closed - ignore
                } catch (e: Exception) {
                    // Other exceptions - ignore to prevent crashes
                }
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback)
            callbackRegistered = true
            
            // Send initial state
            try {
                trySend(isCurrentlyConnected())
            } catch (e: ClosedSendChannelException) {
                // Channel closed before we could send - ignore
            }
        } catch (e: Exception) {
            // Registration failed - close the channel
            close(e)
        }
        
        awaitClose {
            if (callbackRegistered) {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (e: Exception) {
                    // Ignore errors during unregistration
                }
            }
        }
    }.distinctUntilChanged()
    
    /**
     * Check if currently connected to internet
     */
    fun isCurrentlyConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Get network type if connected
     */
    fun getNetworkType(): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return "none"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "unknown"
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            else -> "unknown"
        }
    }
}
