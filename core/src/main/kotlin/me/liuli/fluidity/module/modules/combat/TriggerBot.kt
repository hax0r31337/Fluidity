/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PreMotionEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.module.special.CombatManager
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.ClickTimer
import me.liuli.fluidity.util.world.getDistanceToEntityBox
import me.liuli.fluidity.util.world.rayTraceEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemSword

class TriggerBot : Module("TriggerBot", "Automatically attack the target you view", ModuleCategory.COMBAT) {

    private val minCpsValue by IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue by IntValue("MaxCPS", 12, 1, 20)
    private val swingItemValue by BoolValue("SwingItem", true)
    private val rayCastValue by BoolValue("RayCast", false)
    private val noBlockAttacksValue by ListValue("NoBlockAttacks", arrayOf("Always", "Tick", "None"), "None")
    private val autoBlockValue by ListValue("AutoBlock", arrayOf("Vanilla", "Hurt", "None"), "None")
    private val autoBlockPacketValue by ListValue("AutoBlockPacket", arrayOf("Vanilla", "Packet"), "Vanilla")

    private val clickTimer = ClickTimer()
    private var lastBlocked = false

    override fun onEnable() {
        clickTimer.update(minCpsValue, maxCpsValue)
        lastBlocked = false
    }

    override fun onDisable() {
        if (lastBlocked) {
            unblockSword()
        }
    }

    private fun rayTraceTarget(): Entity? {
        return if (!rayCastValue) {
            mc.objectMouseOver.entityHit
        } else {
            rayTraceEntity(Reach.reach)
        }
    }

    @Listen
    fun onPreMotion(event: PreMotionEvent) {
        // autoblock
        val canBlock = mc.thePlayer.heldItem?.item is ItemSword && when(autoBlockValue) {
            "Vanilla" -> mc.theWorld.loadedEntityList.any { mc.thePlayer.getDistanceToEntityBox(it) < Reach.reach && it.isTarget(true) }
            "Hurt" -> {
                val target = mc.theWorld.loadedEntityList.find { mc.thePlayer.getDistanceToEntityBox(it) < Reach.reach && it.isTarget(true) }
                if (target != null && target is EntityLivingBase) {
                    target.hurtTime in 3..9
                } else false
            }
            else -> false
        }
        if (canBlock) {
            blockSword()
            if (!lastBlocked && noBlockAttacksValue == "Tick") {
                lastBlocked = true
                return
            }
        } else {
            if (lastBlocked) {
                lastBlocked = false
                unblockSword()
                if (!lastBlocked && noBlockAttacksValue == "Tick") return
            }
        }

        if (noBlockAttacksValue == "Always" && mc.thePlayer.itemInUseCount != 0) return

        if (clickTimer.canClick()) {
            val target = rayTraceTarget()
            if (target != null && target.isTarget()) {
                // attack
                if (swingItemValue) {
                    mc.thePlayer.swingItem()
                }
                mc.playerController.attackEntity(mc.thePlayer,  target)
                clickTimer.update(minCpsValue, maxCpsValue)
            }
        }
    }

    private fun blockSword() {
        when(autoBlockPacketValue) {
            "Vanilla" -> mc.gameSettings.keyBindUseItem.pressed = true
            "Packet" -> CombatManager.blockSword()
        }
    }

    private fun unblockSword() {
        when(autoBlockPacketValue) {
            "Vanilla" -> mc.gameSettings.keyBindUseItem.pressed = false
            "Packet" -> CombatManager.unblockSword()
        }
    }
}