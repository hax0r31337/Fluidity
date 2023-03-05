/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.util.ChatAllowedCharacters

class ReportBot : Module("ReportBot", "report players automatically", ModuleCategory.MISC) {

    private val modeValue by ListValue("Mode", arrayOf("DoMCer"), "DoMCer")
    private val delayValue by IntValue("Delay", 5000, 1000, 10000)

    private val delayTimer = TheTimer()
    private val reportedList = mutableListOf<String>()

    override fun onDisable() {
        reportedList.clear()
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (delayTimer.hasTimePassed(delayValue)) {
            mc.netHandler.playerInfoMap.forEach {
                val name = it.gameProfile.name
                if(name != mc.session.username) {
                    if (doReport(name)) {
                        delayTimer.reset()
                        return
                    }
                }
            }
        }
    }

    private fun doReport(name: String): Boolean {
        if (reportedList.contains(name)) return false

        name.forEach {
            if (!ChatAllowedCharacters.isAllowedCharacter(it)) return false
        }

        if (modeValue == "DoMCer") {
            mc.thePlayer.sendChatMessage("/report $name")
        }
        reportedList.add(name)
        return true
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S2DPacketOpenWindow) {
            val name = packet.windowTitle
            if (name.unformattedText.startsWith("举报")) {
                displayAlert(name.unformattedText)
                mc.netHandler.addToSendQueue(C0EPacketClickWindow(packet.windowId, 11, 0, 0, ItemStack(Items.diamond_sword), 8))
                event.cancel()
            }
        }
    }
}