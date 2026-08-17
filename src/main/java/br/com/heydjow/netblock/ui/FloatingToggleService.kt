package br.com.heydjow.netblock.ui

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.*
import br.com.heydjow.netblock.R
import br.com.heydjow.netblock.data.FilterStatePrefs
import br.com.heydjow.netblock.data.FloatingPrefs
import br.com.heydjow.netblock.vpn.NetBlockVpnService
import br.com.heydjow.netblock.vpn.VpnState
import java.util.Locale
import kotlin.math.abs

class FloatingToggleService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var button: View
    private lateinit var params: WindowManager.LayoutParams

    private var startX = 0
    private var startY = 0
    private var touchX = 0f
    private var touchY = 0f

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val (savedX, savedY) = FloatingPrefs.load(this)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = savedX
        params.y = savedY

        button = LayoutInflater.from(this)
            .inflate(R.layout.floating_button, null)

        setupTouch()

        windowManager.addView(button, params)
        handler.post(runnable)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouch() {
        button.setOnTouchListener { _, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - touchX).toInt()
                    params.y = startY + (event.rawY - touchY).toInt()
                    windowManager.updateViewLayout(button, params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    FloatingPrefs.save(this, params.x, params.y)

                    val dx = abs(event.rawX - touchX)
                    val dy = abs(event.rawY - touchY)

                    // Clique curto (não arrasto)
                    if (dx < 10 && dy < 10) {
                        Log.e("NetBlock", "Clique curto")
                        if (NetBlockVpnService.state == NetBlockVpnService.VpnState.STOPPED) {
                            Log.e("NetBlock", "inicia")
                            // 🔵 VPN ainda não ativa → inicia
                            NetBlockVpnService.start(this)
                        } else {
                            Log.e("NetBlock", "reinicia")
                            // 🔘 VPN ativa → toggle filtro
                            FilterStatePrefs.toggle(this)
                            NetBlockVpnService.restart(applicationContext)
                        }

                        updateState()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun updateState() {
        /*button.alpha =
            if (FilterStatePrefs.isEnabled(this) && VpnState.isVpnActive())
                1f
            else
                0.4f*/
        //button.alpha = 1f
        button.setBackgroundResource(
            if (FilterStatePrefs.isEnabled(this) && VpnState.isVpnActive())
                R.drawable.floating_bg_on
            else
                R.drawable.floating_bg_off
        )
    }

    override fun onDestroy() {
        if (::button.isInitialized) {
            windowManager.removeView(button)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            updateState()
            handler.postDelayed(this, 1_000)
        }
    }
}
