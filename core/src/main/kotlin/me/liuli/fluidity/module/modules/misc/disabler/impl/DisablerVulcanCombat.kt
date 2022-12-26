/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.misc.disabler.impl

import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.event.WorldEvent
import me.liuli.fluidity.module.modules.misc.disabler.DisablerMode
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import java.util.*
import kotlin.math.abs

class DisablerVulcanCombat : DisablerMode("VulcanCombat") {

    private var currentTrans = 0
    private var vulTickCounterUID = 0
    private val packetBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
    private val packetBuffer1 = mutableMapOf<Long, Packet<INetHandlerPlayServer>>()
    private val lagTimer = TheTimer()

    override fun onEnable() {
        vulTickCounterUID = -25767
    }

    override fun onUpdate(event: UpdateEvent) {
        if(lagTimer.hasTimePassed(5000L) && packetBuffer.size > 4) {
            lagTimer.reset()
            while (packetBuffer.size > 4) {
                mc.netHandler.addToSendQueue(packetBuffer.peek())
                packetBuffer.poll()
            }
        }
        val now = System.currentTimeMillis()
        packetBuffer1.map { it }.forEach { (ts, packet) ->
            if (ts + 2000L > now) {
                mc.netHandler.addToSendQueue(packet)
                packetBuffer1.remove(ts)
            }
        }
    }

    override fun onWorld(event: WorldEvent) {
        currentTrans = 0
        packetBuffer.clear()
        lagTimer.reset()
        vulTickCounterUID = -25767
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0FPacketConfirmTransaction && !packetBuffer.contains(packet)) {
            if (abs((abs((packet.uid).toInt()) - abs(vulTickCounterUID))) <= 4) {
                vulTickCounterUID = (packet.uid).toInt()
                packetBuffer.add(packet)
                event.cancel()
//                displayAlert("RECV ${packetBuffer.size}")
            }else if (abs((abs((packet.uid).toInt()) - 25767)) <= 4) {
                vulTickCounterUID = (packet.uid).toInt()
//                displayAlert("RST")
            }
            packetBuffer1[System.currentTimeMillis()] = packet
        }
    }
}