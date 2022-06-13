package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S02PacketChat

class AutoMiningBoost : Module("AutoMiningBoost", "Automatically use Hypixel SkyBlock mining boost", ModuleCategory.PLAYER) {

    @EventMethod
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S02PacketChat) {
            if (packet.chatComponent.unformattedText.startsWith("Mining Speed Boost is now available!", true)) {
                if (mc.thePlayer.heldItem.unlocalizedName.contains("Pickaxe", true)) {
                    mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                    displayAlert("Mining Speed Boost used!")
                } else {
                    displayAlert("Mining Speed Boost ignored!")
                }
            }
        }
    }
}