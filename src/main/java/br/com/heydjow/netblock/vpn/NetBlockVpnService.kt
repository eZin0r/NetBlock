package br.com.heydjow.netblock.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import br.com.heydjow.netblock.R
import br.com.heydjow.netblock.data.FilterStatePrefs
import br.com.heydjow.netblock.data.SelectedAppsPrefs
import java.lang.Thread.sleep
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat

class NetBlockVpnService : VpnService() {

    private var tun: ParcelFileDescriptor? = null
    private var thread: Thread? = null

    enum class VpnState {
        STOPPED,
        STARTING,
        RUNNING,
        STOPPING
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notifyState()
        when (intent?.action) {
            ACTION_START -> {
                if (state == VpnState.STOPPED) {
                    startVpn()
                }
            }

            ACTION_STOP -> {
                if (state == VpnState.RUNNING || state == VpnState.STARTING) {
                    stopVpn()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return Service.START_NOT_STICKY
                }
            }

            ACTION_RESTART -> {
                if (state == VpnState.RUNNING || state == VpnState.STARTING) {
                    stopVpn()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    startVpn()
                    val restartIntent = Intent(this, NetBlockVpnService::class.java)
                        .setAction(ACTION_START)

                    startForegroundService(restartIntent)
                }
            }
            ACTION_TOGGLE_FILTER -> {
                FilterStatePrefs.setEnabled(
                    this,
                    !FilterStatePrefs.isEnabled(this)
                )
                restart(this)
            }
            else -> {
                // Sistema pode reiniciar o service sem action
                if (state == VpnState.STOPPED) {
                    startVpn()
                }
            }
        }

        //startVpn()
        //isRunning = true
        return Service.START_STICKY
    }

    private fun startVpn() {
        state = VpnState.STARTING
        notifyState()
        startForeground(NOTIFICATION_ID, buildNotification())
        tun?.close()
        thread?.interrupt()

        val builder = Builder()
            .setSession("NetBlock")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")

        val allApps = packageManager.getInstalledPackages(0)
            .map { it.packageName }
            .toSet()

        var allowed = allApps

        if (FilterStatePrefs.isEnabled(applicationContext)) {

            // 🔥 Apps selecionados devem ser BLOQUEADOS
            val blocked = FilterStatePrefs.getSelectedPackages(applicationContext)

            Log.d("NetBlock", "Apps BLOQUEADOS (addAllowed): $blocked")

            allowed = allApps - blocked

            blocked.forEach { pkg ->
                try {
                    // ⚠️ ENTRA NA VPN → SEM INTERNET
                    builder.addAllowedApplication(pkg)
                } catch (_: Exception) {}
            }
        }

        //if (allowed.contains("com.app99.driver"))
        //    Log.i("NetBlock", "APP99")

        allowed.forEach { pkg ->
            try {
                // ⚠️ NÃO ENTRA NA VPN → TEM INTERNET
                builder.addDisallowedApplication(pkg)
            } catch (_: Exception) {}
        }

        tun = builder.establish()

        tun?.let {
            thread = Thread(TunLoop(this, it), "NetBlockTun")
            thread?.start()
            state = VpnState.RUNNING
            notifyState()
            updateNotification()
        }
    }

    override fun onDestroy() {
        thread?.interrupt()
        tun?.close()
        super.onDestroy()
    }

    private fun stopVpn() {
        state = VpnState.STOPPING
        notifyState()
        try {
            thread?.interrupt()
            (thread as? TunLoop)?.stop()
        } catch (_: Exception) {}

        try {
            tun?.close()   // 🔥 ISSO É O QUE REALMENTE PARA A VPN
        } catch (_: Exception) {}

        thread = null
        tun = null
        state = VpnState.STOPPED
        notifyState()
        updateNotification()
    }
    private fun notifyState() {
        sendBroadcast(
            Intent(ACTION_VPN_STATE)
                .putExtra(EXTRA_STATE, state.name)
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NetBlock VPN",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Controle da VPN NetBlock"
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
    private fun actionIntent(action: String): PendingIntent {
        return PendingIntent.getService(
            this,
            action.hashCode(),
            Intent(this, NetBlockVpnService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private fun contentToggleIntent(): PendingIntent {
        return PendingIntent.getService(
            this,
            0,
            Intent(this, NetBlockVpnService::class.java)
                .setAction(ACTION_TOGGLE_FILTER),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private fun updateNotification() {
        if (state != VpnState.RUNNING) return
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification())
    }
    private fun buildNotification(): Notification {
        val running = state == VpnState.RUNNING
        val filterEnabled = FilterStatePrefs.isEnabled(this)
        val text = when {
            !running -> "VPN desativada"
            filterEnabled -> "🔴 Bloqueio ativo"
            else -> "🟢 Bloqueio desativado"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn)
            .setContentTitle("NetBlock VPN")
            .setContentText(text)
            .setContentIntent(contentToggleIntent())
            .setOngoing(true)
            .addAction(
                R.drawable.ic_power,
                if (running) "Desativar" else "Ativar",
                actionIntent(if (running) ACTION_STOP else ACTION_START)
            )
            .addAction(
                R.drawable.ic_block,
                if (filterEnabled) "Liberar" else "Bloquear",
                actionIntent(ACTION_TOGGLE_FILTER)
            )
            /*.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1) // 👈 MOSTRA NO MODO RECOLHIDO
            )*/
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    companion object {
        const val CHANNEL_ID = "netblock_vpn"
        const val NOTIFICATION_ID = 1001

        const val ACTION_TOGGLE_FILTER = "br.com.heydjow.netblock.TOGGLE_FILTER"
        const val ACTION_VPN_STATE = "br.com.heydjow.netblock.VPN_STATE"
        const val EXTRA_STATE = "state"
        const val ACTION_START = "br.com.heydjow.netblock.START"
        const val ACTION_STOP = "br.com.heydjow.netblock.STOP"
        const val ACTION_RESTART = "br.com.heydjow.netblock.RESTART"

        var state = VpnState.STOPPED
            private set

        fun restart(ctx: Context) {
            ctx.startForegroundService(
                Intent(ctx, NetBlockVpnService::class.java)
                    .setAction(ACTION_RESTART)
            )
        }

        fun start(ctx: Context) {
            ctx.startForegroundService(
                Intent(ctx, NetBlockVpnService::class.java)
                    .setAction(ACTION_START)
            )
        }

        fun stop(ctx: Context) {
            ctx.startForegroundService(
                Intent(ctx, NetBlockVpnService::class.java)
                    .setAction(ACTION_STOP)
            )
        }
    }
}
