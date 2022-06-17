package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2FPacketSetSlot

class AutoMelody : Module("AutoMelody", "Automatically plays Hypixel SkyBlock melody", ModuleCategory.PLAYER) {

    @EventMethod
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S2FPacketSetSlot) {
            // TODO: fix connected notes
            val slot = packet.func_149173_d()
            val windowId = packet.func_149175_c()
            val item = packet.func_149174_e()
            if (windowId == 0 || item == null || item.unlocalizedName != "tile.quartzBlock.default") {
                return
            }
            // check if the item is highlighted
            val dn = item.displayName
            if (dn.length < 5) {
                return
            }
            val color = dn.substring(0, 2)
            if (dn.replace(color, "") == "| Click!") {
                val transId = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)
                mc.netHandler.addToSendQueue(C0EPacketClickWindow(windowId, slot, 0, 0, item, transId))
            }
        }
    }
}