package br.com.heydjow.netblock.vpn

import android.content.Context
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InterruptedIOException

class TunLoop(
    private val ctx: Context,
    tun: ParcelFileDescriptor
) : Runnable {

    private val input = FileInputStream(tun.fileDescriptor)
    private val output = FileOutputStream(tun.fileDescriptor)

    @Volatile
    private var running = true

    fun stop() {
        running = false
        try {
            input.close()
        } catch (_: Exception) {}
        try {
            output.close()
        } catch (_: Exception) {}
    }

    override fun run() {
        val buffer = ByteArray(32767)

        try {
            while (running && !Thread.currentThread().isInterrupted) {
                val len = input.read(buffer)
                if (len > 0) {
                    output.write(buffer, 0, len)
                }
            }
        } catch (e: InterruptedIOException) {
            // ✅ encerramento normal da VPN
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stop()
        }
    }
}
