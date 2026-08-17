package br.com.heydjow.netblock.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import br.com.heydjow.netblock.R
import android.provider.Settings
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.heydjow.netblock.data.FilterStatePrefs
import br.com.heydjow.netblock.model.SelectedApp
import br.com.heydjow.netblock.ui.adapter.SelectedAppsAdapter
import br.com.heydjow.netblock.vpn.NetBlockVpnService
import androidx.recyclerview.widget.RecyclerView
import br.com.heydjow.netblock.vpn.NetBlockVpnService.VpnState
import android.os.Build

class MainActivity : Activity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnVpn: Button
    private lateinit var btnApps: Button

    private var vpnState = VpnState.STOPPED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVpn = findViewById(R.id.btnVpn)
        btnApps = findViewById(R.id.btnApps)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnVpn.setOnClickListener { toggleVpn() }

        btnVpn.post {
            Log.i(
                "BTNVPN",
                "visibility=${btnVpn.visibility} enabled=${btnVpn.isEnabled} " +
                        "width=${btnVpn.width} height=${btnVpn.height} " +
                        "x=${btnVpn.x} y=${btnVpn.y}"
            )
        }

        btnApps.setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        // Overlay
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        startService(Intent(this, FloatingToggleService::class.java))
        vpnState = NetBlockVpnService.state
        updateVpnButton()
    }

    override fun onResume() {
        super.onResume()

        // ✅ registra receiver APENAS aqui
        registerReceiver(
            vpnStateReceiver,
            IntentFilter(NetBlockVpnService.ACTION_VPN_STATE),
            Context.RECEIVER_EXPORTED
        )

        // 🔥 sincroniza estado REAL da VPN
        vpnState = NetBlockVpnService.state
        updateVpnButton()

        loadSelectedApps()
    }

    override fun onPause() {
        unregisterReceiver(vpnStateReceiver)
        super.onPause()
    }

    // 🔘 Botão único
    private fun toggleVpn() {

        // Evita spam enquanto está mudando estado
        if (vpnState == VpnState.STARTING || vpnState == VpnState.STOPPING) {
            return
        }

        when (vpnState) {

            VpnState.STOPPED -> {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 100)
                } else {
                    NetBlockVpnService.start(this)
                }
            }

            VpnState.RUNNING -> {
                NetBlockVpnService.stop(this)
            }

            else -> {
                // STARTING / STOPPING → ignora
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            startOrStopVpn()
        }
    }

    private fun startOrStopVpn() {
        Log.i("NetBlock", "VPN state: $vpnState")
        when (vpnState) {
            VpnState.STOPPED -> NetBlockVpnService.start(this)
            VpnState.RUNNING -> NetBlockVpnService.stop(this)
            else -> Unit
        }
    }

    private fun updateVpnButton() {
        btnVpn.text = when (vpnState) {
            VpnState.STOPPED -> "Ativar VPN"
            VpnState.STARTING -> "Iniciando VPN…"
            VpnState.RUNNING -> "Desativar VPN"
            VpnState.STOPPING -> "Desativando VPN…"
        }

        btnVpn.isEnabled =
            vpnState == VpnState.STOPPED || vpnState == VpnState.RUNNING
    }

    private fun loadSelectedApps() {
        val pkgs = FilterStatePrefs.getSelectedPackages(applicationContext)
        val pm = packageManager

        val apps = pkgs.mapNotNull {
            try {
                val info = pm.getApplicationInfo(it, 0)
                SelectedApp(it, pm.getApplicationLabel(info).toString())
            } catch (_: Exception) {
                null
            }
        }

        recyclerView.adapter = SelectedAppsAdapter(apps.toMutableList())
    }

    // 📡 Receiver
    private val vpnStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val s = intent?.getStringExtra(NetBlockVpnService.EXTRA_STATE)
            vpnState = VpnState.valueOf(s ?: VpnState.STOPPED.name)
            updateVpnButton()

            Log.i("NetBlock", "VPN state atualizado: $vpnState")
        }
    }
}
