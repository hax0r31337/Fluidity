/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PacketEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.init.Items
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction

class AutoConsume : Module("AutoConsume", "Automatically consume items.", ModuleCategory.PLAYER) {

    private val modeValue by ListValue("Mode", arrayOf("Soup", "Head", "Wand"), "Soup")
    private val healthValue by FloatValue("Health", 10F, 1F, 20F)
    private val delayValue by IntValue("Delay", 1000, 0, 10000)
    private val noConsumeHitValue by BoolValue("NoConsumeHit", false)

    private var consumed = false
    private var prevSlot = -1
    private val timer = TheTimer()

    override fun onDisable() {
        consumed = false
        prevSlot = -1
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (!timer.hasTimePassed(delayValue)) {
            return
        }

        if (consumed && prevSlot != -1) { // recover
            mc.thePlayer.inventory.currentItem = prevSlot
            prevSlot = -1
            consumed = false
            timer.reset()
        } else if (!consumed && prevSlot == -1) { // not eat
            if (mc.thePlayer.health > healthValue) {
                return
            }
            val slot = searchItem()
            if (slot == -1) return // no item in inventory
            prevSlot = mc.thePlayer.inventory.currentItem
            mc.thePlayer.inventory.currentItem = slot
        } else if (!consumed && prevSlot != -1) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
            consumed = true
        }
    }

    @Listen
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (noConsumeHitValue && prevSlot != -1 && packet is C0BPacketEntityAction) {
            event.cancel()
        }
    }

    private fun searchItem(): Int {
        for (i in 36 until 45) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && ((modeValue == "Soup" && stack.item === Items.mushroom_stew)
                        || (modeValue == "Head" && stack.item === Items.skull
                        || (modeValue == "Wand" && stack.displayName.contains("Wand of", true))))) {
                return i - 36
            }
        }
        return -1
    }
}