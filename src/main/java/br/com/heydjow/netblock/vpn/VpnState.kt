package br.com.heydjow.netblock.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object VpnState {
    fun isVpnActive(): Boolean {
        return NetBlockVpnService.state == NetBlockVpnService.VpnState.RUNNING
    }
}
