package br.com.heydjow.netblock.vpn
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class FilterVpnService : VpnService() {
    private var tun: ParcelFileDescriptor? = null
    override fun onStartCommand(i:Intent?,f:Int,s:Int):Int {
        if (tun==null) start()
        return START_STICKY
    }
    private fun start() {
        tun = Builder().addAddress("10.0.0.2",32)
            .addRoute("0.0.0.0",0)
            .setSession("NetBlock")
            .establish()
        Thread(TunLoop(this,tun!!)).start()
    }
}