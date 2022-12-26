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
import me.liuli.fluidity.util.client.displayAlert
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.ClickTimer
import me.liuli.fluidity.util.world.getDistanceToEntityBox
import me.liuli.fluidity.util.world.rayTraceEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition

class TriggerBot : Module("TriggerBot", "Automatically attack the target you view", ModuleCategory.COMBAT) {

    private val minCpsValue = IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue = IntValue("MaxCPS", 12, 1, 20)
    private val swingItemValue = BoolValue("SwingItem", true)
    private val rayCastValue = BoolValue("RayCast", false)
    private val throughWallsValue = BoolValue("ThroughWalls", false)
    private val noBlockAttacksValue = ListValue("NoBlockAttacks", arrayOf("Always", "Tick", "None"), "None")
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Vanilla", "Hurt", "None"), "None")
    private val autoBlockPacketValue = ListValue("AutoBlockPacket", arrayOf("Vanilla", "Packet"), "Vanilla")

    private val clickTimer = ClickTimer()
    private var lastBlocked = false

    override fun onEnable() {
        clickTimer.update(minCpsValue.get(), maxCpsValue.get())
        lastBlocked = false
    }

    override fun onDisable() {
        if (lastBlocked) {
            unblockSword()
        }
    }

    private fun rayTraceTarget(): Entity? {
        val target = rayTraceEntity(Reach.reach) { it.isTarget(true) } ?: return null
        if (!throughWallsValue.get() && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return null

        return if (rayCastValue.get()) {
            return rayTraceEntity(Reach.reach).also {
                if (target != it) {
                    displayAlert(it.toString())
                }
            }
        } else target
    }

    @Listen
    fun onPreMotion(event: PreMotionEvent) {
        // autoblock
        val canBlock = mc.thePlayer.heldItem?.item is ItemSword && when(autoBlockValue.get()) {
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
            if (!lastBlocked && noBlockAttacksValue.get() == "Tick") {
                lastBlocked = true
                return
            }
        } else {
            if (lastBlocked) {
                lastBlocked = false
                unblockSword()
                if (!lastBlocked && noBlockAttacksValue.get() == "Tick") return
            }
        }

        if (noBlockAttacksValue.get() == "Always" && mc.thePlayer.itemInUseCount != 0) return

        if (clickTimer.canClick()) {
            val target = rayTraceTarget()
            if (target != null) {
                // attack
                if (swingItemValue.get()) {
                    mc.thePlayer.swingItem()
                }
                mc.playerController.attackEntity(mc.thePlayer,  target)
                clickTimer.update(minCpsValue.get(), maxCpsValue.get())
            }
        }
    }

    private fun blockSword() {
        when(autoBlockPacketValue.get()) {
            "Vanilla" -> mc.gameSettings.keyBindUseItem.pressed = true
            "Packet" -> CombatManager.blockSword()
        }
    }

    private fun unblockSword() {
        when(autoBlockPacketValue.get()) {
            "Vanilla" -> mc.gameSettings.keyBindUseItem.pressed = false
            "Packet" -> CombatManager.unblockSword()
        }
    }
}