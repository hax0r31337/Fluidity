/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.Render3DEvent
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.jitterRotation
import me.liuli.fluidity.util.timing.ClickTimer
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSword

class AutoClicker : Module("AutoClicker", "Constantly clicks when holding down a mouse button.", category = ModuleCategory.COMBAT) {

    private val minCpsValue = IntValue("MinCPS", 7, 1, 20)
    private val maxCpsValue = IntValue("MaxCPS", 12, 1, 20)

    private val leftValue = BoolValue("Left", true)
    private val leftSwordOnlyValue = BoolValue("LeftSwordOnly", false)
    private val rightValue = BoolValue("Right", true)
    private val rightBlockOnlyValue = BoolValue("RightBlockOnly", false)
    private val jitterValue = FloatValue("Jitter", 0.0f, 0.0f, 5.0f)

    private val leftClickTimer = ClickTimer()
    private val rightClickTimer = ClickTimer()

    override fun onEnable() {
        leftClickTimer.update(minCpsValue.get(), maxCpsValue.get())
        rightClickTimer.update(minCpsValue.get(), maxCpsValue.get())
    }

    @Listen
    fun onRender(event: Render3DEvent) {
        // Left click
        if (mc.gameSettings.keyBindAttack.pressed && leftValue.get() && (!leftSwordOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemSword) &&
            leftClickTimer.canClick() && mc.playerController.curBlockDamageMP == 0F) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode) // Minecraft Click Handling

            leftClickTimer.update(minCpsValue.get(), maxCpsValue.get())
        }

        // Right click
        if (mc.gameSettings.keyBindUseItem.pressed && !mc.thePlayer!!.isUsingItem &&
            (!rightBlockOnlyValue.get() || mc.thePlayer.heldItem?.item is ItemBlock) && rightValue.get() && rightClickTimer.canClick()) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode) // Minecraft Click Handling

            rightClickTimer.update(minCpsValue.get(), maxCpsValue.get())
        }
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (jitterValue.get() != 0f && (leftValue.get() && mc.gameSettings.keyBindAttack.pressed && mc.playerController.curBlockDamageMP == 0F
                    || rightValue.get() && mc.gameSettings.keyBindUseItem.pressed && !mc.thePlayer.isUsingItem)) {
            val jitter = jitterRotation(jitterValue.get(), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
            mc.thePlayer.rotationYaw = jitter.first
            mc.thePlayer.rotationPitch = jitter.second
        }
    }
}