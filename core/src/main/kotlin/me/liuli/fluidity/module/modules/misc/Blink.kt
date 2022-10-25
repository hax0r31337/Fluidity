/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.util.mc
import net.minecraft.client.gui.GuiDownloadTerrain
import net.minecraft.network.Packet
import java.util.LinkedList
import java.util.Queue

class Blink : Module("Blink", "Suspend packet send and make server thought you were lag", ModuleCategory.MISC) {

    private val packets = LinkedList<Packet<*>>() as Queue<Packet<*>>

    override fun onDisable() {
        sendPackets()
    }

    private fun sendPackets() {
        mc.netHandler ?: return
        while (packets.isNotEmpty()) {
            mc.netHandler.addToSendQueue(packets.poll())
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        if (mc.currentScreen is GuiDownloadTerrain) {
            packets.clear()
            state = false
        } else if (event.type == PacketEvent.Type.SEND) {
            packets.offer(event.packet)
            event.cancel()
        }
    }
}