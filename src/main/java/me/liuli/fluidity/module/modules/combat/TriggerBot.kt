package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PreMotionEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.ClickTimer
import me.liuli.fluidity.util.world.getDistanceToEntityBox
import me.liuli.fluidity.util.world.rayTraceEntity
import net.minecraft.entity.Entity
import net.minecraft.item.ItemSword

class TriggerBot : Module("TriggerBot", "Automatically attack the target you view", ModuleCategory.COMBAT) {

    private val minCpsValue = IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue = IntValue("MaxCPS", 12, 1, 20)
    private val swingItemValue = BoolValue("SwingItem", true)
    private val rayTraceValue = ListValue("RayTrace", arrayOf("Vanilla", "ThroughWall"), "Vanilla")
    private val autoBlockValue = BoolValue("AutoBlock", false)

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
        if (autoBlockValue.get()) {
            if (mc.thePlayer.heldItem?.item is ItemSword && mc.theWorld.loadedEntityList.any { mc.thePlayer.getDistanceToEntityBox(it) < Reach.reach && it.isTarget(true) }) {
                lastBlocked = true
                mc.gameSettings.keyBindUseItem.pressed = true
            } else {
                if (lastBlocked) {
                    lastBlocked = false
                    mc.gameSettings.keyBindUseItem.pressed = false
                }
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