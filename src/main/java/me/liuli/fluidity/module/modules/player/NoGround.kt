package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import net.minecraft.network.play.client.C03PacketPlayer

class NoGround : Module("NoGround", "Don't send onGround packets.", ModuleCategory.PLAYER) {

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = false
            mc.thePlayer.onGround = mc.gameSettings.keyBindJump.isPressed && mc.thePlayer.onGround
//            displayAlert("${mc.thePlayer.onGround}")
        }
    }
}