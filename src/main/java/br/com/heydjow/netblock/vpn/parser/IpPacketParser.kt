package br.com.heydjow.netblock.vpn.parser

import java.nio.ByteBuffer
import java.nio.ByteOrder

object IpPacketParser {

    const val PROTO_TCP = 6
    const val PROTO_UDP = 17

    data class PacketInfo(
        val protocol: Int,
        val srcPort: Int
    )

    fun parse(buffer: ByteArray, length: Int): PacketInfo? {
        if (length < 20) return null

        val bb = ByteBuffer.wrap(buffer, 0, length)
        bb.order(ByteOrder.BIG_ENDIAN)

        val versionIhl = bb.get(0).toInt()
        val version = versionIhl shr 4
        if (version != 4) return null

        val ihl = (versionIhl and 0x0F) * 4
        val protocol = bb.get(9).toInt() and 0xFF

        bb.position(ihl)

        return when (protocol) {
            PROTO_TCP -> {
                if (length < ihl + 20) null
                else PacketInfo(protocol, bb.short.toInt() and 0xFFFF)
            }

            PROTO_UDP -> {
                if (length < ihl + 8) null
                else PacketInfo(protocol, bb.short.toInt() and 0xFFFF)
            }

            else -> null
        }
    }
}
