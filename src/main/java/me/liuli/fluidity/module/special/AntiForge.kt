package me.liuli.fluidity.module.special

import io.netty.buffer.Unpooled
import me.liuli.fluidity.config.ConfigManager
import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.Listener
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.util.mc
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload

class AntiForge : Listener {

    @EventMethod
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.isIntegratedServerRunning) {
            return
        }

        if (packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket") {
            event.cancel()
        } else if (packet is C17PacketCustomPayload) {
            if (!packet.channelName.startsWith("MC|")) {
                event.cancel()
            } else if (packet.channelName.equals("MC|Brand", true)) {
                packet.data = PacketBuffer(Unpooled.buffer()).writeString("vanilla")
            }
        }
    }

    override fun listen() = ConfigManager.antiForge
}