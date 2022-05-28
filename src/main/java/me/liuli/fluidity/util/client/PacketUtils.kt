package me.liuli.fluidity.util.client

import me.liuli.fluidity.util.mc
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.server.*

object PacketUtils {

    fun getPacketType(packet: Packet<*>): PacketType {
        val className = packet.javaClass.simpleName
        if (className.startsWith("C", ignoreCase = true)) {
                return PacketType.CLIENTSIDE
        } else if (className.startsWith("S", ignoreCase = true)) {
                return PacketType.SERVERSIDE
        }
        // idk...
        return PacketType.UNKNOWN
    }

    enum class PacketType {
        SERVERSIDE,
        CLIENTSIDE,
        UNKNOWN
    }
}