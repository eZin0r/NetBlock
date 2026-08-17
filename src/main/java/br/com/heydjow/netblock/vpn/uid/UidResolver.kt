package br.com.heydjow.netblock.vpn.uid

import java.io.BufferedReader
import java.io.FileReader

object UidResolver {

    private const val TCP = "/proc/net/tcp"
    private const val UDP = "/proc/net/udp"

    fun resolveUid(port: Int, protocol: Int): Int? {
        val path = if (protocol == 6) TCP else UDP
        BufferedReader(FileReader(path)).useLines { lines ->
            lines.drop(1).forEach { line ->
                val cols = line.trim().split(Regex("\\s+"))
                if (cols.size < 8) return@forEach

                val local = cols[1]
                val uid = cols[7].toIntOrNull() ?: return@forEach

                val localPort = local.split(":")[1].toInt(16)
                if (localPort == port) return uid
            }
        }
        return null
    }
}
