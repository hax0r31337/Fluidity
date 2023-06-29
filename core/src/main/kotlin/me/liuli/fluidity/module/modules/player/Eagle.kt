/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.player

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.PreMotionEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.direction
import me.liuli.fluidity.util.timing.TheTimer
import net.minecraft.util.BlockPos
import kotlin.math.cos
import kotlin.math.sin

class Eagle : Module("Eagle", "Auto sneak on block eagle.", ModuleCategory.PLAYER) {
    private val eagleDistance by FloatValue("EagleDistance", 0.15f, 0.01f, 0.4f)
    private val releaseSneakAfter by IntValue("ReleaseSneakAfter", 200, 0, 1000)

    private val timer = TheTimer()

    override fun onEnable() {
        timer.reset()
    }

    @Listen
    fun onPreMotion(e: PreMotionEvent) {
        mc.gameSettings.keyBindSneak.pressed = checkBlockState() || !timer.hasTimePassed(releaseSneakAfter)

        if (checkBlockState()) {
            timer.reset()
        }
    }

    private fun checkBlockState() : Boolean {
        val yaw = mc.thePlayer.direction

        val checkPoint1 = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
        val checkPoint2 = BlockPos(
            mc.thePlayer.posX - (-sin(yaw) * eagleDistance),
            mc.thePlayer.posY - 1.0,
            mc.thePlayer.posZ - (cos(yaw) * eagleDistance)
        )

        return mc.theWorld.isAirBlock(checkPoint1) || mc.theWorld.isAirBlock(checkPoint2)
    }
}