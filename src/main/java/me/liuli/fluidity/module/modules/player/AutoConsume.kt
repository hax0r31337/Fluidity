package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import net.minecraft.init.Items
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class AutoConsume : Module("AutoConsume", "Automatically consume items.", ModuleCategory.PLAYER) {

    private val modeValue = ListValue("Mode", arrayOf("Soup", "Head"), "Soup")
    private val healthValue = FloatValue("Health", 10F, 1F, 20F)

    private var consumed = false
    private var prevSlot = -1

    override fun onDisable() {
        consumed = false
        prevSlot = -1
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (consumed && prevSlot != -1) { // recover
            mc.thePlayer.inventory.currentItem = prevSlot
            prevSlot = -1
            consumed = false
        } else if (!consumed && prevSlot == -1) { // not eat
            if (mc.thePlayer.health > healthValue.get()) {
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

    private fun searchItem(): Int {
        for (i in 36 until 45) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && ((modeValue.get() == "Soup" && stack.item === Items.mushroom_stew)
                        || (modeValue.get() == "Head" && stack.item === Items.skull))) {
                return i - 36
            }
        }
        return -1
    }
}