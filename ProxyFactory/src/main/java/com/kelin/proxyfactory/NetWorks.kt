package com.kelin.proxyfactory

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.ContextCompat
import com.kelin.logger.Logger

/**
 * **描述:** 网络相关工具。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2024/6/26 15:42
 *
 * **版本:** v 1.0.0
 */
object NetWorks {

    /**
     * 判断网络是否可用。
     */
    var isNetworkAvailable = true
        private set
        get() = if (ProxyFactory.isDebugMode) field else field && isNotVpn

    /**
     * 判断是否开启了VPN。
     */
    var isNotVpn = true

    internal fun init(context: Application, vpnCheck: Boolean) {
        ContextCompat.getSystemService(context, ConnectivityManager::class.java).also { service ->
            if (service != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    service.registerDefaultNetworkCallback(NetworkCallbackImpl(vpnCheck))
                } else {
                    service.registerNetworkCallback(
                        NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .build(),
                        NetworkCallbackImpl(vpnCheck)
                    )
                }
            }
        }
    }

    private class NetworkCallbackImpl(private val vpnCheck: Boolean) : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            isNetworkAvailable = false
            Logger.system("NetWorks")?.i("网络连接已断开")
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val notVpn = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            if (vpnCheck) {
                isNotVpn = notVpn
            }
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                isNetworkAvailable = true
                Logger.system("NetWorks")?.i("网络连接已恢复，VPN开启:${!notVpn}")
            }
        }
    }
}