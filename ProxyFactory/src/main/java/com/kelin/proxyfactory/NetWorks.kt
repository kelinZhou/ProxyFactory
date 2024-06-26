package com.kelin.proxyfactory

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
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
    var isNetworkAvailable = true
        private set

    internal fun init(context: Application) {
        ContextCompat.getSystemService(context, ConnectivityManager::class.java)?.also { service ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                service.unregisterNetworkCallback(object : ConnectivityManager.NetworkCallback() {
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
                })
            } else {
                isNetworkAvailable = service.activeNetworkInfo?.isConnected != false
            }
        }
    }
}