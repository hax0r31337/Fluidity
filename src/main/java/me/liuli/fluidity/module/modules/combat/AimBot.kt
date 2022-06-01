package me.liuli.fluidity.module.modules.combat

import me.liuli.fluidity.event.EventMethod
import me.liuli.fluidity.event.UpdateEvent
import me.liuli.fluidity.module.Module
import me.liuli.fluidity.module.ModuleCategory
import me.liuli.fluidity.module.modules.client.Targets
import me.liuli.fluidity.module.value.BoolValue
import me.liuli.fluidity.module.value.FloatValue
import me.liuli.fluidity.module.value.ListValue
import me.liuli.fluidity.util.mc
import me.liuli.fluidity.util.move.*
import me.liuli.fluidity.util.other.nextFloat
import me.liuli.fluidity.util.world.getDistanceToEntityBox
import net.minecraft.entity.EntityLivingBase
import kotlin.math.floor
import kotlin.random.Random

class AimBot : Module("AimBot", "Helps you aim on your targets", ModuleCategory.COMBAT) {

    private val rangeValue = FloatValue("Range", 4.5f, 1f, 10f)
    private val minTurnSpeedValue = FloatValue("MinTurnSpeed", 10f, 1F, 180F)
    private val maxTurnSpeedValue = FloatValue("MaxTurnSpeed", 50f, 1F, 180F)
    private val priorityValue = ListValue("Priority", arrayOf("Health", "Distance", "Fov", "LivingTime", "Armor", "HurtResistantTime"), "Distance")
    private val fovValue = FloatValue("FOV", 180F, 1F, 180F)
    private val jitterValue = FloatValue("Jitter", 0.0f, 0.0f, 5.0f)
    private val throughWallsValue = BoolValue("ThroughWalls", false)
    private val onlyHoldMouseValue = BoolValue("OnlyHoldMouse", true)
    private val silentRotationValue = BoolValue("SilentRotation", false)

    private val playerRotation: Pair<Float, Float>
        get() = Pair(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

    private var hasTarget = false
    private var needAimBack = false

    override fun onEnable() {
        hasTarget = false
        needAimBack = false
    }

    private fun getRotation(): Pair<Float, Float>{
        hasTarget = false

        if (onlyHoldMouseValue.get() && !mc.gameSettings.keyBindAttack.isKeyDown) return playerRotation

        val entity = mc.theWorld.loadedEntityList
            .filter {
                Targets.isTarget(it, true) && (throughWallsValue.get() || mc.thePlayer.canEntityBeSeen(it)) &&
                        mc.thePlayer.getDistanceToEntityBox(it) <= rangeValue.get() && getRotationDifference(it) <= fovValue.get()
            }.map { it as EntityLivingBase }.let { targets ->
                when (priorityValue.get()) {
                    "Distance" -> targets.minByOrNull { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
                    "Health" -> targets.minByOrNull { it.health } // Sort by health
                    "Fov" -> targets.minByOrNull { getRotationDifference(it) } // Sort by FOV
                    "LivingTime" -> targets.minByOrNull { -it.ticksExisted } // Sort by existence
                    "Armor" -> targets.minByOrNull { it.totalArmorValue } // Sort by armor
                    "HurtResistantTime" -> targets.minByOrNull { it.hurtResistantTime } // Sort by armor
                    else -> targets.firstOrNull()
                }
            } ?: return playerRotation

        val boundingBox = entity.entityBoundingBox ?: return playerRotation

        hasTarget = true
        needAimBack = true
        return toRotation(getCenter(boundingBox), true)
    }

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        val destinationRotation = getRotation()

        if (!needAimBack) return

        // Figure out the best turn speed suitable for the distance and configured turn speed
        val rotationDiff = getRotationDifference(lastReportedYaw, lastReportedPitch, destinationRotation.first, destinationRotation.second)

        var (yaw, pitch) = limitAngleChange(lastReportedYaw, lastReportedPitch, destinationRotation.first, destinationRotation.second,
            ((rotationDiff / 180) * maxTurnSpeedValue.get() + (1 - rotationDiff / 180) * minTurnSpeedValue.get()).toFloat())

        if (floor(yaw) == floor(mc.thePlayer.rotationYaw) && floor(pitch) == floor(mc.thePlayer.rotationPitch)) {
            needAimBack = false
            if (silentRotationValue.get()) return
        }

        if (hasTarget && jitterValue.get() != 0f) {
            if (Random.nextBoolean()) yaw += nextFloat(-jitterValue.get(), jitterValue.get())

            if (Random.nextBoolean()) {
                pitch += nextFloat(-jitterValue.get(), jitterValue.get())

                // Make sure pitch is not going into unlegit values
                if (pitch > 90)
                    pitch = 90F
                else if (pitch < -90)
                    pitch = -90F
            }
        }

        // fix GCD sensitivity to bypass some anti-cheat measures
        fixSensitivity(yaw, pitch).also {
            yaw = it.first
            pitch = it.second
        }

        if (silentRotationValue.get()) {
            setServerRotation(yaw, pitch)
        } else {
            mc.thePlayer.rotationYaw = yaw
            mc.thePlayer.rotationPitch = pitch
        }
    }
}