package com.kelin.proxyfactory

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
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

    var isNetworkAvailable = true
        private set
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            field
        } else {
            ContextCompat.getSystemService(ProxyFactory.getContext(), ConnectivityManager::class.java)?.activeNetworkInfo?.isConnected != false
        }

    @Suppress("Deprecation")
    internal fun init(context: Application) {
        ContextCompat.getSystemService(context, ConnectivityManager::class.java).also { service ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && service != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    service.registerDefaultNetworkCallback(NetworkCallbackImpl())
                } else {
                    service.registerNetworkCallback(
                        NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .build(),
                        NetworkCallbackImpl()
                    )
                }
            } else {
                context.registerReceiver(NetworkStateBroadcastReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            isNetworkAvailable = false
            Logger.system("ProxyFactory")?.i("网络连接已断开")
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            Logger.system("ProxyFactory")?.i("代理开启情况：${networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)}")
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                isNetworkAvailable = true
                Logger.system("ProxyFactory")?.i("网络连接已恢复")
            }
        }
    }

    private class NetworkStateBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.also { action ->
                Logger.system("ProxyFactory")?.i("网络广播：${intent.extras}")
            }
        }
    }
}