/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.misc

import me.liuli.fluidity.event.AttackEvent
import me.liuli.fluidity.event.ClickBlockEvent
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.world.getAttackDamage
import net.minecraft.item.*

class AutoItems : Module("AutoItems", "Automatically switch items", ModuleCategory.MISC) {

    private val attackValue = BoolValue("Attack", true)
    private val attackOnlySwordValue = BoolValue("AttackOnlySword", true)
    private val mineBlockValue = BoolValue("MineBlock", true)
    private val placeBlockValue = BoolValue("PlaceBlock", false)

    @Listen
    fun onAttack(event: AttackEvent) {
        if (!attackValue.get())
            return

        // Find best weapon in hotbar (#Kotlin Style)
        val (slot, _) = (0..8)
            .map { Pair(it, mc.thePlayer.inventory.getStackInSlot(it)) }
            .filter { it.second != null && (it.second.item is ItemSword || (it.second.item is ItemTool && !attackOnlySwordValue.get())) }
            .maxByOrNull { it.second.getAttackDamage() } ?: return
        swapItem(slot)
    }

    @Listen
    fun onClick(event: ClickBlockEvent) {
        if (event.type == ClickBlockEvent.Type.LEFT && mineBlockValue.get()) {
            var bestSpeed = 1F
            var bestSlot = -1

            val block = mc.theWorld.getBlockState(event.clickedBlock)?.block ?: return

            for (i in 0..8) {
                val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
                val speed = item.getStrVsBlock(block)

                if (speed > bestSpeed) {
                    bestSpeed = speed
                    bestSlot = i
                }
            }

            if (bestSlot != -1) {
                swapItem(bestSlot)
            }
        } else if (event.type == ClickBlockEvent.Type.RIGHT && placeBlockValue.get()) {
            val cur = mc.thePlayer.inventory.getCurrentItem()?.item
            if (cur != null && (cur is ItemBlock || cur is ItemFood || cur is ItemPotion)) {
                return
            }

            val (slot, _) = (0..8)
                .map { Pair(it, mc.thePlayer.inventory.getStackInSlot(it)) }
                .filter { it.second?.item is ItemBlock && it.second?.stackSize ?: 0 > 0 }
                .maxByOrNull { it.second?.stackSize ?: 0 } ?: return
            swapItem(slot)
        }
    }

    private fun swapItem(slot: Int) {
        if (slot == mc.thePlayer.inventory.currentItem) { // If in hand no need to swap
            return
        }

        // swap items
        mc.thePlayer.inventory.currentItem = slot
        mc.playerController.updateController()
    }
}