package me.liuli.fluidity.util.client

import net.minecraft.network.Packet
import net.minecraft.network.play.server.S12PacketEntityVelocity

val S12PacketEntityVelocity.realMotionX: Float
    get() = motionX / 8000f

val S12PacketEntityVelocity.realMotionY: Float
    get() = motionY / 8000f

val S12PacketEntityVelocity.realMotionZ: Float
    get() = motionZ / 8000f

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