package me.liuli.fluidity.module.modules.movement

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.StrafeEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.silentRotationYaw
import net.minecraft.util.MathHelper

class RotationStrafe : Module("RotationStrafe", "Make you move \"correctly\"", ModuleCategory.MOVEMENT) {

    private val modeValue = ListValue("Mode", arrayOf("Strict", "Simple"), "Strict")

    @EventMethod
    fun onStrafe(event: StrafeEvent) {
        if (silentRotationYaw.isNaN()) return
        val yaw = silentRotationYaw

        when(modeValue.get()) {
            "Strict" -> {
                var strafe = event.strafe
                var forward = event.forward
                val friction = event.friction

                var f = strafe * strafe + forward * forward

                if (f >= 1.0E-4F) {
                    f = MathHelper.sqrt_float(f)

                    if (f < 1.0F) {
                        f = 1.0F
                    }

                    f = friction / f
                    strafe *= f
                    forward *= f

                    val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                    val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                    mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                    mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                }
                event.cancel()
            }
            "Simple" -> {
                val dif = ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw -
                        23.5f - 135) +
                        180) / 45).toInt()

                val strafe = event.strafe
                val forward = event.forward
                val friction = event.friction

                var calcForward = 0f
                var calcStrafe = 0f

                when (dif) {
                    0 -> {
                        calcForward = forward
                        calcStrafe = strafe
                    }
                    1 -> {
                        calcForward += forward
                        calcStrafe -= forward
                        calcForward += strafe
                        calcStrafe += strafe
                    }
                    2 -> {
                        calcForward = strafe
                        calcStrafe = -forward
                    }
                    3 -> {
                        calcForward -= forward
                        calcStrafe -= forward
                        calcForward += strafe
                        calcStrafe -= strafe
                    }
                    4 -> {
                        calcForward = -forward
                        calcStrafe = -strafe
                    }
                    5 -> {
                        calcForward -= forward
                        calcStrafe += forward
                        calcForward -= strafe
                        calcStrafe -= strafe
                    }
                    6 -> {
                        calcForward = -strafe
                        calcStrafe = forward
                    }
                    7 -> {
                        calcForward += forward
                        calcStrafe += forward
                        calcForward -= strafe
                        calcStrafe += strafe
                    }
                }

                if (calcForward > 1f || calcForward < 0.9f && calcForward > 0.3f || calcForward < -1f || calcForward > -0.9f && calcForward < -0.3f) {
                    calcForward *= 0.5f
                }

                if (calcStrafe > 1f || calcStrafe < 0.9f && calcStrafe > 0.3f || calcStrafe < -1f || calcStrafe > -0.9f && calcStrafe < -0.3f) {
                    calcStrafe *= 0.5f
                }

                var d = calcStrafe * calcStrafe + calcForward * calcForward

                if (d >= 1.0E-4f) {
                    d = MathHelper.sqrt_float(d)
                    if (d < 1.0f) d = 1.0f
                    d = friction / d
                    calcStrafe *= d
                    calcForward *= d
                    val yawSin = MathHelper.sin((yaw * Math.PI / 180f).toFloat())
                    val yawCos = MathHelper.cos((yaw * Math.PI / 180f).toFloat())
                    mc.thePlayer.motionX += calcStrafe * yawCos - calcForward * yawSin.toDouble()
                    mc.thePlayer.motionZ += calcForward * yawCos + calcStrafe * yawSin.toDouble()
                }
                event.cancel()
            }
        }
    }
}