package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.MotionEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.timing.ClickTimer
import me.liuli.fluidity.util.world.getDistanceToEntityBox
import net.minecraft.item.ItemSword

class TriggerBot : Module("TriggerBot", "Automatically attack the target you view", ModuleCategory.COMBAT) {

    private val minCpsValue = IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue = IntValue("MaxCPS", 12, 1, 20)
    private val swingItemValue = BoolValue("SwingItem", true)
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

    @EventMethod
    fun onPreMotion(event: MotionEvent) {
        if (clickTimer.canClick() && mc.objectMouseOver?.entityHit != null && Targets.isTarget(mc.objectMouseOver.entityHit, true)) {
            if (swingItemValue.get()) {
                mc.thePlayer.swingItem()
            }
            mc.playerController.attackEntity(mc.thePlayer,  mc.objectMouseOver.entityHit)
            clickTimer.update(minCpsValue.get(), maxCpsValue.get())
        }
        if (autoBlockValue.get()) {
            val reach = if(Reach.state) Reach.combatReachValue.get().toDouble() else 3.0
            if (mc.thePlayer.heldItem?.item is ItemSword && mc.theWorld.loadedEntityList.any { mc.thePlayer.getDistanceToEntityBox(it) < reach && Targets.isTarget(it, true) }) {
                lastBlocked = true
                mc.gameSettings.keyBindUseItem.pressed = true
            } else {
                if (lastBlocked) {
                    lastBlocked = false
                    mc.gameSettings.keyBindUseItem.pressed = false
                }
            }
        }
    }
}