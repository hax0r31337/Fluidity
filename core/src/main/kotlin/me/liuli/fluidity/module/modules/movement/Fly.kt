/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.BlockBBEvent
import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.strafe
import net.minecraft.util.AxisAlignedBB

class Fly : Module("Fly", "Make you like a bird", ModuleCategory.MOVEMENT) {

    private val modeValue by ListValue("Mode", arrayOf("Vanilla", "FakeGround"), "Vanilla")
    private val hSpeedValue by FloatValue("HorizonSpeed", 1.0f, 0.0f, 5.0f)
    private val vSpeedValue by FloatValue("VerticalSpeed", 0.5f, 0.0f, 5.0f)
    private val noClipValue by BoolValue("NoClip", false)

    private var launchY = 0.0

    override fun onEnable() {
        launchY = mc.thePlayer?.posY ?: 0.0
    }

    override fun onDisable() {
        if (noClipValue) {
            mc.thePlayer.noClip = false
        }
        mc.timer.timerSpeed = 1f
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        if (noClipValue) {
            mc.thePlayer.noClip = true
        }
        mc.thePlayer.fallDistance = 0f
        when (modeValue) {
            "Vanilla" -> {
                if (mc.gameSettings.keyBindJump.pressed) {
                    mc.thePlayer.motionY = vSpeedValue.toDouble()
                } else if (mc.gameSettings.keyBindSneak.pressed) {
                    mc.thePlayer.motionY = -vSpeedValue.toDouble()
                } else {
                    mc.thePlayer.motionY = 0.0
                }
                mc.thePlayer.strafe(hSpeedValue)
            }
        }
    }

    @Listen
    fun onBlockBB(event: BlockBBEvent) {
        if (modeValue != "FakeGround") return
        event.list.add(AxisAlignedBB(mc.thePlayer.posX - 0.5, launchY - 1.0, mc.thePlayer.posZ - 0.5,
            mc.thePlayer.posX + 0.5, launchY, mc.thePlayer.posZ + 0.5))
    }
}