/*
 * This file is part of Fluidity Utility Mod.
 * Use of this source code is governed by the GPLv3 license that can be found in the LICENSE file.
 */

package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.Listen
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets.isTarget
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.IntValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.*
import me.liuli.fluidity.util.timing.TheTimer
import me.liuli.fluidity.util.world.getDistanceToEntityBox
import me.liuli.fluidity.util.world.rayTraceEntity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import java.util.*

class AimBot : Module("AimBot", "Helps you aim on your targets", ModuleCategory.COMBAT) {

    private val rangeValue by FloatValue("Range", 4.5f, 1f, 10f)
    private val minYawTurnSpeedValue by FloatValue("MinYawTurnSpeed", 10f, 1F, 180F)
    private val maxYawTurnSpeedValue by FloatValue("MaxYawTurnSpeed", 50f, 1F, 180F)
    private val minPitchTurnSpeedValue by FloatValue("MinPitchTurnSpeed", 10f, 1F, 180F)
    private val maxPitchTurnSpeedValue by FloatValue("MaxPitchTurnSpeed", 50f, 1F, 180F)
    private val backtrackTicksValue by IntValue("BackTrack", 0, 0, 10)
    private val priorityValue by ListValue("Priority", arrayOf("Health", "Distance", "Fov", "LivingTime", "Armor", "HurtResistantTime"), "Distance")
    private val fovValue by FloatValue("FOV", 180F, 1F, 180F)
    private val jitterValue by FloatValue("Jitter", 0.0f, 0.0f, 5.0f)
    private val throughWallsValue by BoolValue("ThroughWalls", false)
    private val onlyHoldMouseValue by BoolValue("OnlyHoldMouse", true)
    private val silentRotationValue by BoolValue("SilentRotation", false)
    private val aimingModeValue by ListValue("AimingMode", arrayOf("Common", "Lock", "PikaNW"), "Common")
    private val keepRotationValue by IntValue("KeepRotation", 1000, 0, 2500)

    private val playerRotation: Pair<Float, Float>
        get() = if (lastAimingAt != null && !lastAimingTimer.hasTimePassed(keepRotationValue)) lastAimingAt!!
                else Pair(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

    private var hasTarget = false
    private var needAimBack = false
    private var lastAimingAt: Pair<Float, Float>? = null
    private var lastAimingTimer = TheTimer()
    private val backtrackQueue = mutableMapOf<Int, Queue<Vec3>>()
    private var pikanwLastYaw = 0f

    override fun onDisable() {
        hasTarget = false
        needAimBack = false
        backtrackQueue.clear()
        lastAimingAt = null
    }

    private fun getCenter(entity: EntityLivingBase): Vec3 {
        // only limit the scope of backtrack to player, that saves performance :)
        if (entity !is EntityPlayer || backtrackTicksValue == 0) return getCenter(entity.entityBoundingBox)

        return backtrackQueue[entity.entityId]?.peek() ?: getCenter(entity.entityBoundingBox)
    }

    private fun getRotation(): Pair<Float, Float>{
        hasTarget = false

        if (onlyHoldMouseValue && !mc.gameSettings.keyBindAttack.pressed) return playerRotation

        val entity = mc.theWorld.loadedEntityList
            .filter {
                it.isTarget(true) && (throughWallsValue || mc.thePlayer.canEntityBeSeen(it)) &&
                        mc.thePlayer.getDistanceToEntityBox(it) <= rangeValue && getRotationDifference(it) <= fovValue
            }.map { it as EntityLivingBase }.let { targets ->
                when (priorityValue) {
                    "Distance" -> targets.minByOrNull { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
                    "Health" -> targets.minByOrNull { it.health } // Sort by health
                    "Fov" -> targets.minByOrNull { getRotationDifference(it) } // Sort by FOV
                    "LivingTime" -> targets.minByOrNull { -it.ticksExisted } // Sort by existence
                    "Armor" -> targets.minByOrNull { it.totalArmorValue } // Sort by armor
                    "HurtResistantTime" -> targets.minByOrNull { it.hurtResistantTime } // Sort by armor
                    else -> targets.firstOrNull()
                }
            } ?: return playerRotation

        hasTarget = true
        needAimBack = true

        val correctAim = toRotation(getCenter(entity), true)
        return when(aimingModeValue) {
            "Lock" -> correctAim
            // TODO: improve
            "Common" -> if (rayTraceEntity(Reach.reach, yaw = correctAim.first, pitch = mc.thePlayer.serverRotationPitch) { it == entity } != null) {
                Pair(correctAim.first, mc.thePlayer.serverRotationPitch)
            } else correctAim
            "PikaNW" -> {
                val hurt = ((1 - (entity.hurtTime / 10f)) * 1.8f).coerceAtMost(1f).let {
                    if (it == 1f) 0f else it
                }
                if (hurt == 0f) {
                    pikanwLastYaw = correctAim.first
                }
                val yaw = hurt * 60f + if (hurt != 0f) -30f + pikanwLastYaw else ((Math.random().toFloat() - 0.5f) * 3 + correctAim.first)
                Pair(yaw, (correctAim.second + (Math.random().toFloat() - 0.5f) * 3).coerceIn(-89.9f, 89.9f))
            }
            else -> throw IllegalArgumentException("Invalid aiming mode: ${aimingModeValue}")
        }.also {
            lastAimingAt = it
            lastAimingTimer.reset()
        }
    }

    @Listen
    fun onUpdate(event: UpdateEvent) {
        val destinationRotation = getRotation()

        if (!needAimBack) return

        // Figure out the best turn speed suitable for the distance and configured turn speed
        val rotationDiff = getRotationDifference(lastReportedYaw, lastReportedPitch, destinationRotation.first, destinationRotation.second)

        var (yaw, pitch) = limitAngleChange(lastReportedYaw, lastReportedPitch, destinationRotation.first, destinationRotation.second,
            ((rotationDiff / 180) * maxYawTurnSpeedValue + (1 - rotationDiff / 180) * minYawTurnSpeedValue).toFloat(),
            ((rotationDiff / 180) * maxPitchTurnSpeedValue + (1 - rotationDiff / 180) * minPitchTurnSpeedValue).toFloat())

        if (yaw.toInt() == mc.thePlayer.rotationYaw.toInt() && pitch.toInt() == mc.thePlayer.rotationPitch.toInt()) {
            needAimBack = false
            if (silentRotationValue) return
        }

        if (hasTarget && jitterValue != 0f) {
            jitterRotation(jitterValue, yaw, pitch).also {
                yaw = it.first
                pitch = it.second
            }
        }

        if (silentRotationValue) {
            setServerRotation(yaw, pitch)
        } else {
            setClientRotation(yaw, pitch)
        }

        try {
            updateBacktrack()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun updateBacktrack() {
        val t = backtrackTicksValue
        if (t == 0) return
        backtrackQueue.keys.map { it }.forEach {
            if (mc.theWorld.getEntityByID(it) == null) {
                backtrackQueue.remove(it)
            }
        }
        mc.theWorld.loadedEntityList.filterIsInstance<EntityPlayer>().forEach {
            val q = backtrackQueue[it.entityId] ?: LinkedList<Vec3>().also { q -> backtrackQueue[it.entityId] = q }
            q.offer(getCenter(it.entityBoundingBox))
            if (q.size > t) q.poll()
        }
    }
}