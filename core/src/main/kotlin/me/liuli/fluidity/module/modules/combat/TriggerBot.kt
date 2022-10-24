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

    private val minCpsValue = IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue = IntValue("MaxCPS", 12, 1, 20)
    private val swingItemValue = BoolValue("SwingItem", true)
    private val rayTraceValue = ListValue("RayTrace", arrayOf("Vanilla", "ThroughWall"), "Vanilla")
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Vanilla", "Hurt", "None"), "None")

    private val clickTimer = ClickTimer()
    private var lastBlocked = false

    override fun onEnable() {
        clickTimer.update(minCpsValue.get(), maxCpsValue.get())
        lastBlocked = false
    }

    override fun onDisable() {
        if (lastBlocked) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }
    }

    private fun rayTraceTarget(): Entity? {
        return when(rayTraceValue.get()) {
            "ThroughWall" -> rayTraceEntity(Reach.reach)
            else -> mc.objectMouseOver?.entityHit
        }
    }

    @Listen
    fun onPreMotion(event: PreMotionEvent) {
        // autoblock
        val canBlock = mc.thePlayer.heldItem?.item is ItemSword && when(autoBlockValue.get()) {
            "Vanilla" -> mc.theWorld.loadedEntityList.any { mc.thePlayer.getDistanceToEntityBox(it) < Reach.reach && it.isTarget(true) }
            "Hurt" -> {
                val target = mc.theWorld.loadedEntityList.find { mc.thePlayer.getDistanceToEntityBox(it) < Reach.reach && it.isTarget(true) }
                if (target != null && target is EntityLivingBase) {
                    ((1 - (target.hurtTime / target.maxHurtTime.toFloat())) * 1.7f).coerceAtMost(1f).let { if (it == 1f) 0f else it } != 0f
                } else false
            }
            else -> false
        }
        if (canBlock) {
            lastBlocked = true
            mc.gameSettings.keyBindUseItem.pressed = true
        } else {
            if (lastBlocked) {
                lastBlocked = false
                mc.gameSettings.keyBindUseItem.pressed = false
            }
        }

        if (clickTimer.canClick()) {
            val target = rayTraceTarget() ?: return
            if (!target.isTarget(true)) {
                return
            }
            // attack
            if (swingItemValue.get()) {
                mc.thePlayer.swingItem()
            }
            mc.playerController.attackEntity(mc.thePlayer,  target)
            clickTimer.update(minCpsValue.get(), maxCpsValue.get())
        }
    }
}