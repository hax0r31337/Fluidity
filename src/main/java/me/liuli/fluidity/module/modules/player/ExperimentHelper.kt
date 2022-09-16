package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.stripColor
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.play.server.S30PacketWindowItems

class ExperimentHelper : Module("ExperimentHelper", "Helps you do Hypixel SkyBlock experiments", ModuleCategory.PLAYER) {

    private val clicks = mutableListOf<Int>()
    private var chroRemember = false
    private val timer = TheTimer()

    override fun onDisable() {
        clicks.clear()
        chroRemember = false
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (!chroRemember && clicks.isNotEmpty() && timer.hasTimePassed(700)) {
            val chest = mc.currentScreen as? GuiChest ?: return
            if (!chest.lowerChestInventory.displayName.unformattedText.let{ it.contains("Chronomatron (") || it.contains("Ultrasequencer (") }) return
            val idx = clicks.removeFirst()
            mc.netHandler.addToSendQueue(C0EPacketClickWindow(chest.inventorySlots.windowId, idx, 0, 0, chest.lowerChestInventory.getStackInSlot(idx), mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)))
            timer.reset()
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S2FPacketSetSlot) {
            val window = packet.func_149175_c()
            if (window == 0) return
            val inv = (mc.currentScreen as? GuiChest ?: return).lowerChestInventory
            val title = inv.displayName.unformattedText
            val slot = packet.func_149173_d()
            val item = packet.func_149174_e()
            if (title.contains("Chronomatron (")) {
                if (slot >= 54) return
                if (item?.isItemEnchanted == true && inv.getStackInSlot(slot)?.isItemEnchanted == false && timer.hasTimePassed(50) && chroRemember) {
                    timer.reset()
                    clicks.add(slot)
                } else if (item != null) {
                    if (item.displayName.contains("Remember the pattern!")) {
                        chroRemember = true
                        clicks.clear()
                    } else if (item.displayName.contains("Timer:")) {
                        chroRemember = false
                    }
                }
            } else if (title.contains("Ultrasequencer (")) {
                if (item != null) {
                    if (item.displayName.contains("Timer:")) {
                        chroRemember = false
                    }
                }
            } /* else if (title.contains("Superpairs (")) {
                if (slot >= 54) return
                if (item?.displayName?.contains("Click any button!") == true) {
                    event.cancel()
                    println(slot)
                }
            } */
        } else if (packet is S2DPacketOpenWindow) {
            if (packet.windowTitle?.unformattedText?.contains("Chronomatron (") == true) {
                chroRemember = true
                clicks.clear()
            }
        } else if (packet is S30PacketWindowItems) {
            val window = packet.func_148911_c()
            if (window == 0) return
            val inv = (mc.currentScreen as? GuiChest ?: return).lowerChestInventory
            val title = inv.displayName.unformattedText
            if (title.contains("Ultrasequencer (")) {
                val counts = mutableMapOf<Int, Int>()
                packet.itemStacks.forEachIndexed { i, it ->
                    it ?: return@forEachIndexed
                    if (it.displayName.contains("Remember the pattern!")) {
                        chroRemember = true
                        clicks.clear()
                    } else {
                        val str = stripColor(it.displayName).trim()
                        try {
                            counts[str.toInt()] = i
                        } catch (e: Exception) {}
                    }
                }
                if (chroRemember) {
                    counts.entries.sortedBy { it.key }.forEach {
                        clicks.add(it.value)
                    }
                }
            }
        }
    }
}