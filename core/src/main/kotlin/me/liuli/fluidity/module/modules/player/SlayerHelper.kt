package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.ModuleManager
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.render.stripColor
import me.liuli.fluidity.util.world.getScoreboardCollection
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.event.ClickEvent
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S02PacketChat

class SlayerHelper : Module("SlayerHelper", "Auto purchase for slayer quest", ModuleCategory.PLAYER) {

    private val moduleStateMap = mutableMapOf<Module, Boolean>()
    private var stage = 0
    private var slayerType = ""

    override fun onEnable() {
        moduleStateMap.clear()
        ModuleManager.getModule("TriggerBot")?.also {
            moduleStateMap[it] = it.state
        }
        ModuleManager.getModule("AimBot")?.also {
            moduleStateMap[it] = it.state
        }
        ModuleManager.getModule("AutoMobs")?.also {
            moduleStateMap[it] = it.state
        }
        stage = 0
        slayerType = ""
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.ticksExisted % 5 != 0) return
        when(stage) {
            0 -> {
                var slayerStage = ""
                getScoreboardCollection().also {
                    it.forEach { str ->
                        val raw = stripColor(str)
                        if (raw.contains("Slayer Quest")) {
                            val idx = it.indexOf(str)
                            slayerType = stripColor(it[idx-1].trim()).replace("\uD83D\uDC79", "")
                            slayerStage = (it[idx-2].trim()).replace("\uD83D\uDC79", "")
                        }
                    }
                }
                if (!slayerStage.contains("Boss slain!", true) || slayerType == "") {
                    return
                }
                displayAlert(slayerType)
                moduleStateMap.keys.map { it }.forEach {
                    moduleStateMap[it] = it.state
                    it.state = false
                }
                for (i in 36..44) {
                    val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue
                    if (stripColor(itemStack.displayName).contains("Maddox", true)) {
                        mc.thePlayer.inventory.currentItem = i - 36
                        stage = 1
                        return
                    }
                }
                displayAlert("ERROR 0")
            }
            1 -> {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
                stage = 2
            }
            3 -> {
                if (clickItem { stripColor(it.displayName).contains("Complete", true) } == -1) {
                    stage = 4
                }
            }
            4 -> {
                val cont = mc.currentScreen as? GuiChest ?: return
                if (cont.lowerChestInventory.displayName.unformattedText.contains(slayerType.split(" ")[0], true)) {
                    stage = 5
                    return
                }

                if (clickItem("Slayer") { stripColor(it.displayName).contains(slayerType.split(" ")[0]) } == -1) {
                    restoreSession()
                    displayAlert("ERROR 2")
                }
            }
            5 -> {
                val cont = mc.currentScreen as? GuiChest ?: return
                if (cont.lowerChestInventory.displayName.unformattedText.contains("Confirm", true)) {
                    stage = 6
                    return
                }

                if (clickItem(slayerType.split(" ")[0]) { stripColor(it.displayName).contains(slayerType.split(" ").last()) } == -1) {
                    restoreSession()
                    displayAlert("ERROR 3")
                }
            }
            6 -> {
                if (mc.currentScreen !is GuiChest) {
                    restoreSession()
                    return
                }

                if (clickItem("Confirm") { stripColor(it.displayName).contains("Confirm", true) } == -1) {
                    restoreSession()
                    displayAlert("ERROR 4")
                }
            }
        }
    }

    private fun restoreSession() {
        moduleStateMap.forEach {
            it.key.state = it.value
        }
        slayerType = ""
        stage = 0
        mc.thePlayer.inventory.currentItem = 0
    }

    private fun clickItem(expectedName: String = "", func: (ItemStack) -> (Boolean)): Int {
        val cont = mc.currentScreen as? GuiChest ?: return 0
        if (!cont.lowerChestInventory.displayName.unformattedText.contains(expectedName, true)) {
            return 0
        }
        cont.inventorySlots.inventory.forEachIndexed { index, it ->
            it ?: return@forEachIndexed
            if (func(it)) {
                mc.netHandler.addToSendQueue(C0EPacketClickWindow(cont.inventorySlots.windowId, index, 0, 0, it, mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)))
                return 1
            }
        }

        return -1
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (stage == 2 && packet is S02PacketChat) {
            val component = packet.chatComponent
            component.siblings.forEach { sib ->
                val clickEvent = sib.chatStyle.chatClickEvent
                if(clickEvent != null && clickEvent.action == ClickEvent.Action.RUN_COMMAND && clickEvent.value.startsWith("/cb")) {
                    mc.netHandler.addToSendQueue(C01PacketChatMessage(clickEvent.value))
                    stage = 3
                }
            }
        }
    }
}